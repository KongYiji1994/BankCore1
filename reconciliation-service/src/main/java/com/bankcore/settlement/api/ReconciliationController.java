package com.bankcore.settlement.api;

import com.bankcore.settlement.model.ReconciliationResult;
import com.bankcore.settlement.model.ReconciliationSummaryEntity;
import com.bankcore.settlement.service.ReconciliationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/recon")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReconciliationResult upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "reconDate", required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reconDate) throws IOException {
        return reconciliationService.uploadAndReconcile(file, reconDate);
    }

    @GetMapping("/summary/latest")
    public ReconciliationResult latest() {
        return reconciliationService.latestSummary();
    }

    @GetMapping("/summary/{id}")
    public ReconciliationResult summary(@PathVariable("id") Long id) {
        return reconciliationService.summaryById(id);
    }

    @GetMapping("/summary")
    public List<ReconciliationSummaryEntity> summaryByDate(@RequestParam("date")
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reconciliationService.findSummariesForDate(date);
    }

    @GetMapping("/summary/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable("id") Long id) {
        ReconciliationResult result = reconciliationService.summaryById(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String header = "instruction_id,break_type,internal_amount,external_amount,currency,remark\n";
        outputStream.write(header.getBytes(StandardCharsets.UTF_8), 0, header.length());
        if (result.getBreaks() != null) {
            for (int i = 0; i < result.getBreaks().size(); i++) {
                String line = buildLine(result.getBreaks().get(i));
                outputStream.write(line.getBytes(StandardCharsets.UTF_8), 0, line.length());
            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recon-" + id + ".csv");
        return ResponseEntity.ok().headers(headers).body(outputStream.toByteArray());
    }

    private String buildLine(com.bankcore.settlement.model.ReconciliationBreak reconciliationBreak) {
        StringBuilder builder = new StringBuilder();
        builder.append(reconciliationBreak.getInstructionId()).append(',');
        builder.append(reconciliationBreak.getBreakType().name()).append(',');
        builder.append(reconciliationBreak.getInternalAmount() == null ? "" : reconciliationBreak.getInternalAmount().toPlainString()).append(',');
        builder.append(reconciliationBreak.getExternalAmount() == null ? "" : reconciliationBreak.getExternalAmount().toPlainString()).append(',');
        builder.append(reconciliationBreak.getCurrency()).append(',');
        builder.append(reconciliationBreak.getRemark() == null ? "" : reconciliationBreak.getRemark());
        builder.append('\n');
        return builder.toString();
    }
}
