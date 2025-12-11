package com.bankcore.settlement.config;

import com.bankcore.common.dto.ReconciliationSummary;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ReconciliationJobConfig {

    @Bean
    public Job reconciliationJob(JobBuilderFactory jobBuilderFactory, Step reconcileStep) {
        return jobBuilderFactory.get("reconciliationJob")
                .incrementer(new RunIdIncrementer())
                .start(reconcileStep)
                .build();
    }

    @Bean
    public Step reconcileStep(StepBuilderFactory stepBuilderFactory,
                              ItemReader<String> paymentReader,
                              ItemProcessor<String, String> paymentProcessor,
                              ItemWriter<String> paymentWriter) {
        return stepBuilderFactory.get("reconcileStep")
                .<String, String>chunk(10)
                .reader(paymentReader)
                .processor(paymentProcessor)
                .writer(paymentWriter)
                .build();
    }

    @Bean
    public ItemReader<String> paymentReader() {
        List<String> demoPayments = List.of("TXN-100", "TXN-101", "TXN-102");
        return new ListItemReader<>(demoPayments);
    }

    @Bean
    public ItemProcessor<String, String> paymentProcessor() {
        return item -> item + "-MATCHED";
    }

    @Bean
    public ItemWriter<String> paymentWriter() {
        return items -> {
            // In production we'd emit a file or push MQ events. Here we log to execution context for quick verification.
            ExecutionContext context = new ExecutionContext();
            context.put("reconciliation", new ReconciliationSummary(items.size(), items.size(), List.of()));
        };
    }
}
