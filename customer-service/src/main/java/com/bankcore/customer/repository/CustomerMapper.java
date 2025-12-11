package com.bankcore.customer.repository;

import com.bankcore.customer.model.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface CustomerMapper {
    @Select("SELECT customer_id, name, credit_code, contact_name, contact_phone, onboard_date, risk_level, status, segment FROM customers")
    List<Customer> findAll();

    @Select("SELECT customer_id, name, credit_code, contact_name, contact_phone, onboard_date, risk_level, status, segment FROM customers WHERE customer_id = #{id}")
    Customer findById(@Param("id") String id);

    @Insert("INSERT INTO customers(customer_id, name, credit_code, contact_name, contact_phone, onboard_date, risk_level, status, segment) VALUES(#{customerId}, #{name}, #{creditCode}, #{contactName}, #{contactPhone}, #{onboardDate}, #{riskLevel}, #{status}, #{segment})")
    void insert(Customer customer);
}
