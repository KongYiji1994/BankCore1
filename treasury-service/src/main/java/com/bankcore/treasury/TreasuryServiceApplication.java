package com.bankcore.treasury;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.bankcore")
@EnableScheduling
@MapperScan("com.bankcore.treasury.repository")
public class TreasuryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TreasuryServiceApplication.class, args);
    }
}
