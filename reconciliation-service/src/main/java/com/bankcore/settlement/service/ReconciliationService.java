package com.bankcore.settlement.service;

import com.bankcore.settlement.model.ExternalPaymentRecord;
import com.bankcore.settlement.model.PaymentRecord;
import com.bankcore.settlement.model.ReconciliationBreak;
import com.bankcore.settlement.model.ReconciliationBreakType;
import com.bankcore.settlement.model.ReconciliationResult;
import com.bankcore.settlement.model.ReconciliationSummaryEntity;
import com.bankcore.settlement.repository.PaymentMapper;
import com.bankcore.settlement.repository.ReconciliationBreakMapper;
import com.bankcore.settlement.repository.ReconciliationSummaryMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对账服务：处理外部对账文件导入、比对内外流水、生成差异及汇总报表。
 */
@Service
public class ReconciliationService {

    private final PaymentMapper paymentMapper;
    private final ReconciliationBreakMapper breakMapper;
    private final ReconciliationSummaryMapper summaryMapper;
    private final AsyncTaskExecutor reconciliationTaskExecutor;

    /**
     * 构造注入对账涉及的仓储。
     */
    public ReconciliationService(PaymentMapper paymentMapper,
                                 ReconciliationBreakMapper breakMapper,
                                 ReconciliationSummaryMapper summaryMapper,
                                 @Qualifier("reconciliationTaskExecutor") AsyncTaskExecutor reconciliationTaskExecutor) {
        this.paymentMapper = paymentMapper;
        this.breakMapper = breakMapper;
        this.summaryMapper = summaryMapper;
        this.reconciliationTaskExecutor = reconciliationTaskExecutor;
    }

    /**
     * 上传外部文件并执行对账，生成差异明细与汇总。
     */
    @Transactional
    public ReconciliationResult uploadAndReconcile(MultipartFile file, LocalDate reconDate) throws IOException {
        LocalDate targetDate = reconDate != null ? reconDate : LocalDate.now();
        List<ExternalPaymentRecord> externalRecords = parseExternalFile(file);

        List<PaymentRecord> internalPayments = paymentMapper.findPaymentsForDate(targetDate);
        ConcurrentHashMap<String, PaymentRecord> internalMap = new ConcurrentHashMap<String, PaymentRecord>();
        for (PaymentRecord record : internalPayments) {
            internalMap.put(record.getInstructionId(), record);
        }

        List<Future<ReconciliationSliceResult>> futures = new ArrayList<Future<ReconciliationSliceResult>>();
        int batchSize = Math.max(1000, externalRecords.size() / 16); // 动态分片，兼顾小文件与超大文件
        for (int i = 0; i < externalRecords.size(); i += batchSize) {
            int end = Math.min(i + batchSize, externalRecords.size());
            List<ExternalPaymentRecord> slice = externalRecords.subList(i, end);
            futures.add(reconciliationTaskExecutor.submit(new ReconciliationSliceTask(slice, internalMap, targetDate)));
        }

        AtomicInteger matched = new AtomicInteger();
        AtomicInteger externalOnly = new AtomicInteger();
        AtomicInteger amountMismatch = new AtomicInteger();
        List<ReconciliationBreak> breaks = Collections.synchronizedList(new ArrayList<ReconciliationBreak>());

        for (Future<ReconciliationSliceResult> future : futures) {
            try {
                ReconciliationSliceResult sliceResult = future.get();
                matched.addAndGet(sliceResult.matched);
                externalOnly.addAndGet(sliceResult.externalOnly);
                amountMismatch.addAndGet(sliceResult.amountMismatch);
                breaks.addAll(sliceResult.breaks);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IOException("Reconciliation interrupted", ie);
            } catch (ExecutionException ee) {
                throw new IOException("Reconciliation execution failed", ee);
            }
        }

        int internalOnly = 0;
        for (PaymentRecord remaining : internalMap.values()) {
            internalOnly++;
            breaks.add(buildBreak(remaining, null, ReconciliationBreakType.INTERNAL_ONLY, targetDate));
        }

        ReconciliationSummaryEntity summary = new ReconciliationSummaryEntity();
        summary.setFileName(file.getOriginalFilename());
        summary.setReconDate(targetDate);
        summary.setTotalCount(externalRecords.size() + internalOnly);
        summary.setMatchedCount(matched.get());
        summary.setExternalOnlyCount(externalOnly.get());
        summary.setInternalOnlyCount(internalOnly);
        summary.setAmountMismatchCount(amountMismatch.get());
        summary.setCreatedAt(LocalDateTime.now());
        summaryMapper.insertSummary(summary);

        if (!breaks.isEmpty()) {
            for (ReconciliationBreak reconciliationBreak : breaks) {
                reconciliationBreak.setSummaryId(summary.getId());
            }
            breakMapper.insertBreaks(breaks);
        }

        ReconciliationResult result = new ReconciliationResult();
        result.setSummary(summary);
        result.setBreaks(breakMapper.findBySummaryId(summary.getId()));
        return result;
    }

    /**
     * 查询最近一次对账结果。
     */
    public ReconciliationResult latestSummary() {
        ReconciliationSummaryEntity summary = summaryMapper.findLatest();
        if (summary == null) {
            return null;
        }
        ReconciliationResult result = new ReconciliationResult();
        result.setSummary(summary);
        result.setBreaks(breakMapper.findBySummaryId(summary.getId()));
        return result;
    }

    /**
     * 按主键查询对账汇总与差异。
     */
    public ReconciliationResult summaryById(Long id) {
        ReconciliationSummaryEntity summary = summaryMapper.findById(id);
        if (summary == null) {
            return null;
        }
        ReconciliationResult result = new ReconciliationResult();
        result.setSummary(summary);
        result.setBreaks(breakMapper.findBySummaryId(id));
        return result;
    }

    /**
     * 查询指定日期的对账汇总列表。
     */
    public List<ReconciliationSummaryEntity> findSummariesForDate(LocalDate date) {
        return summaryMapper.findByDate(date);
    }

    /**
     * 解析外部 CSV 对账文件，转换为外部流水列表。
     */
    private List<ExternalPaymentRecord> parseExternalFile(MultipartFile file) throws IOException {
        List<ExternalPaymentRecord> records = new ArrayList<ExternalPaymentRecord>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        CSVParser parser = null;
        try {
            parser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);
            for (CSVRecord csvRecord : parser) {
                ExternalPaymentRecord record = new ExternalPaymentRecord();
                record.setInstructionId(csvRecord.get("instruction_id"));
                record.setExternalReference(csvRecord.get("external_ref"));
                record.setPayerAccount(csvRecord.get("payer_account"));
                record.setPayeeAccount(csvRecord.get("payee_account"));
                record.setCurrency(csvRecord.get("currency"));
                String amountText = csvRecord.get("amount");
                record.setAmount(new BigDecimal(amountText));
                records.add(record);
            }
        } finally {
            if (parser != null) {
                parser.close();
            }
            reader.close();
        }
        return records;
    }

    /**
     * 构造差异记录，描述内外流水缺失或金额不一致的情况。
     */
    private ReconciliationBreak buildBreak(PaymentRecord internal,
                                           ExternalPaymentRecord external,
                                           ReconciliationBreakType type,
                                           LocalDate reconDate) {
        ReconciliationBreak reconciliationBreak = new ReconciliationBreak();
        reconciliationBreak.setBreakType(type);
        reconciliationBreak.setCurrency(external != null ? external.getCurrency() : internal.getCurrency());
        reconciliationBreak.setInstructionId(external != null ? external.getInstructionId() : internal.getInstructionId());
        reconciliationBreak.setExternalReference(external != null ? external.getExternalReference() : null);
        reconciliationBreak.setInternalAmount(internal != null ? internal.getAmount() : null);
        reconciliationBreak.setExternalAmount(external != null ? external.getAmount() : null);
        if (type == ReconciliationBreakType.AMOUNT_MISMATCH) {
            reconciliationBreak.setRemark("Amount or currency mismatch on " + reconDate.toString());
        } else if (type == ReconciliationBreakType.EXTERNAL_ONLY) {
            reconciliationBreak.setRemark("External present without internal payment on " + reconDate.toString());
        } else if (type == ReconciliationBreakType.INTERNAL_ONLY) {
            reconciliationBreak.setRemark("Internal payment missing in external file on " + reconDate.toString());
        }
        return reconciliationBreak;
    }

    /**
     * 分片任务：在固定大小的列表上进行对账对比，返回分段汇总结果。
     */
    private class ReconciliationSliceTask implements Callable<ReconciliationSliceResult> {
        private final List<ExternalPaymentRecord> slice;
        private final ConcurrentHashMap<String, PaymentRecord> internalMap;
        private final LocalDate reconDate;

        ReconciliationSliceTask(List<ExternalPaymentRecord> slice,
                                ConcurrentHashMap<String, PaymentRecord> internalMap,
                                LocalDate reconDate) {
            this.slice = slice;
            this.internalMap = internalMap;
            this.reconDate = reconDate;
        }

        @Override
        public ReconciliationSliceResult call() {
            ReconciliationSliceResult result = new ReconciliationSliceResult();
            for (ExternalPaymentRecord external : slice) {
                PaymentRecord internal = internalMap.remove(external.getInstructionId());
                if (internal == null) {
                    result.externalOnly++;
                    result.breaks.add(buildBreak(null, external, ReconciliationBreakType.EXTERNAL_ONLY, reconDate));
                } else {
                    if (internal.getAmount().compareTo(external.getAmount()) == 0
                            && internal.getCurrency().equalsIgnoreCase(external.getCurrency())) {
                        result.matched++;
                    } else {
                        result.amountMismatch++;
                        result.breaks.add(buildBreak(internal, external, ReconciliationBreakType.AMOUNT_MISMATCH, reconDate));
                    }
                }
            }
            return result;
        }
    }

    /**
     * 分片汇总结果，用于多线程对账后统一汇聚统计。
     */
    private static class ReconciliationSliceResult {
        private int matched;
        private int externalOnly;
        private int amountMismatch;
        private List<ReconciliationBreak> breaks = new ArrayList<ReconciliationBreak>();
    }
}
