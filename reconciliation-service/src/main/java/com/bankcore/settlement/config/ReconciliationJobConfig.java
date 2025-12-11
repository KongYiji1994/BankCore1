package com.bankcore.settlement.config;

import com.bankcore.settlement.model.ReconciliationSummaryEntity;
import com.bankcore.settlement.service.ReconciliationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class ReconciliationJobConfig {

    @Bean
    public Job reconciliationJob(JobBuilderFactory jobBuilderFactory, Step dailyReconStep) {
        return jobBuilderFactory.get("reconciliationJob")
                .incrementer(new RunIdIncrementer())
                .start(dailyReconStep)
                .build();
    }

    @Bean
    public Step dailyReconStep(StepBuilderFactory stepBuilderFactory, ReconciliationService reconciliationService) {
        return stepBuilderFactory.get("dailyReconStep")
                .tasklet((contribution, chunkContext) -> {
                    LocalDate reconDate = LocalDate.now();
                    List<ReconciliationSummaryEntity> summaries = reconciliationService.findSummariesForDate(reconDate);
                    if (summaries == null || summaries.isEmpty()) {
                        chunkContext.getStepContext().getStepExecution().getExecutionContext()
                                .put("reconStatus", "No external file uploaded for " + reconDate.toString());
                    } else {
                        chunkContext.getStepContext().getStepExecution().getExecutionContext()
                                .put("reconStatus", "Summaries exist for " + reconDate.toString());
                    }
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
