package com.bankcore.account.repository;

import com.bankcore.account.model.AccountLedgerEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountLedgerMapper {
    AccountLedgerEntry findByRequestId(@Param("requestId") String requestId);

    void insert(AccountLedgerEntry entry);
}
