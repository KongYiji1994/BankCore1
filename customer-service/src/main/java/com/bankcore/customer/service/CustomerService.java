package com.bankcore.customer.service;

import com.bankcore.customer.model.Customer;
import com.bankcore.customer.repository.CustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerMapper mapper;

    public CustomerService(CustomerMapper mapper) {
        this.mapper = mapper;
    }

    public List<Customer> listCustomers() {
        return mapper.findAll();
    }

    public Customer getCustomer(String id) {
        return mapper.findById(id);
    }

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
