package com.bankcore.account.repository;

import com.bankcore.account.model.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountMapper {
    Account findById(@Param("accountId") String accountId);

    List<Account> findAll();

    void insert(Account account);

    void update(Account account);
}
