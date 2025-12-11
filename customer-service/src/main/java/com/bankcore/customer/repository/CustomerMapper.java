package com.bankcore.customer.repository;

import com.bankcore.customer.model.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface CustomerMapper {
    @Select("SELECT customer_id, name, national_id, onboard_date, risk_level, segment FROM customers")
    List<Customer> findAll();

    @Select("SELECT customer_id, name, national_id, onboard_date, risk_level, segment FROM customers WHERE customer_id = #{id}")
    Customer findById(@Param("id") String id);

    @Insert("INSERT INTO customers(customer_id, name, national_id, onboard_date, risk_level, segment) VALUES(#{customerId}, #{name}, #{nationalId}, #{onboardDate}, #{riskLevel}, #{segment})")
    void insert(Customer customer);
}
