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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReconciliationService {

    private final PaymentMapper paymentMapper;
    private final ReconciliationBreakMapper breakMapper;
    private final ReconciliationSummaryMapper summaryMapper;

    public ReconciliationService(PaymentMapper paymentMapper,
                                 ReconciliationBreakMapper breakMapper,
                                 ReconciliationSummaryMapper summaryMapper) {
        this.paymentMapper = paymentMapper;
        this.breakMapper = breakMapper;
        this.summaryMapper = summaryMapper;
    }

    @Transactional
    public ReconciliationResult uploadAndReconcile(MultipartFile file, LocalDate reconDate) throws IOException {
        LocalDate targetDate = reconDate != null ? reconDate : LocalDate.now();
        List<ExternalPaymentRecord> externalRecords = parseExternalFile(file);

        List<PaymentRecord> internalPayments = paymentMapper.findPaymentsForDate(targetDate);
        Map<String, PaymentRecord> internalMap = new HashMap<String, PaymentRecord>();
        for (PaymentRecord record : internalPayments) {
            internalMap.put(record.getInstructionId(), record);
        }

        List<ReconciliationBreak> breaks = new ArrayList<ReconciliationBreak>();
        int matched = 0;
        int externalOnly = 0;
        int amountMismatch = 0;

        for (ExternalPaymentRecord external : externalRecords) {
            PaymentRecord internal = internalMap.get(external.getInstructionId());
            if (internal == null) {
                externalOnly++;
                breaks.add(buildBreak(null, external, ReconciliationBreakType.EXTERNAL_ONLY, targetDate));
            } else {
                if (internal.getAmount().compareTo(external.getAmount()) == 0
                        && internal.getCurrency().equalsIgnoreCase(external.getCurrency())) {
                    matched++;
                } else {
                    amountMismatch++;
                    breaks.add(buildBreak(internal, external, ReconciliationBreakType.AMOUNT_MISMATCH, targetDate));
                }
                internalMap.remove(external.getInstructionId());
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
        summary.setMatchedCount(matched);
        summary.setExternalOnlyCount(externalOnly);
        summary.setInternalOnlyCount(internalOnly);
        summary.setAmountMismatchCount(amountMismatch);
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

    public List<ReconciliationSummaryEntity> findSummariesForDate(LocalDate date) {
        return summaryMapper.findByDate(date);
    }

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
}
