package com.bankcore.customer.service;

import com.bankcore.customer.model.Customer;
import com.bankcore.customer.repository.CustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 客户与 KYC 服务：负责客户创建、查询及状态维护。
 */
@Service
public class CustomerService {
    private final CustomerMapper mapper;

    /**
     * 构造注入 MyBatis Mapper。
     */
    public CustomerService(CustomerMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询全部客户列表。
     */
    public List<Customer> listCustomers() {
        return mapper.findAll();
    }

    /**
     * 按客户 ID 查询客户详情。
     */
    public Customer getCustomer(String id) {
        return mapper.findById(id);
    }

    /**
     * 创建企业客户并补充 KYC 状态、风控等级。
     */
    @Transactional
    public Customer createCustomer(String name, String creditCode, String contactName, String contactPhone, String segment, String riskLevel, String status) {
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID().toString());
        customer.setName(name);
        customer.setCreditCode(creditCode);
        customer.setContactName(contactName);
        customer.setContactPhone(contactPhone);
        customer.setSegment(segment);
        customer.setRiskLevel(riskLevel == null ? "LOW" : riskLevel);
        customer.setStatus(status == null ? "NORMAL" : status);
        customer.setOnboardDate(LocalDate.now());
        mapper.insert(customer);
        return customer;
    }
}
