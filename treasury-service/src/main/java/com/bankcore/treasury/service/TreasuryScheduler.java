package com.bankcore.treasury.service;

import com.bankcore.treasury.model.CashPoolInterestEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TreasuryScheduler {
    private final CashPoolService cashPoolService;
    private final Logger log = LoggerFactory.getLogger(TreasuryScheduler.class);

    public TreasuryScheduler(CashPoolService cashPoolService) {
        this.cashPoolService = cashPoolService;
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void nightlySweep() {
        log.info("Starting nightly cash pool sweep");
        cashPoolService.sweepAll();
    }

    @Scheduled(cron = "0 30 23 * * ?")
    public void accrueInterest() {
        log.info("Posting daily interest for all pools");
        List<com.bankcore.treasury.model.CashPool> pools = cashPoolService.list();
        for (com.bankcore.treasury.model.CashPool pool : pools) {
            CashPoolInterestEntry entry = cashPoolService.accrueDailyInterest(pool.getPoolId());
            if (entry != null) {
                log.info("Interest posted for pool {} amount {}", pool.getPoolId(), entry.getInterestAmount());
            }
        }
    }
}
