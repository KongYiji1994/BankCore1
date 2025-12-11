package com.bankcore.customer.api;

import com.bankcore.customer.model.Customer;
import com.bankcore.customer.service.CustomerService;
import com.bankcore.common.dto.AccountDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final RestTemplate restTemplate;
    private final String accountServiceBaseUrl;

    public CustomerController(CustomerService customerService, RestTemplate restTemplate,
                              @Value("${account-service.url:http://localhost:8081}") String accountServiceBaseUrl) {
        this.customerService = customerService;
        this.restTemplate = restTemplate;
        this.accountServiceBaseUrl = accountServiceBaseUrl;
    }

    @GetMapping
    public List<Customer> list() {
        return customerService.listCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable String id) {
        Customer customer = customerService.getCustomer(id);
        return customer != null ? ResponseEntity.ok(customer) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Customer> create(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = customerService.createCustomer(request.getName(), request.getCreditCode(),
                request.getContactName(), request.getContactPhone(), request.getSegment(), request.getRiskLevel(), request.getStatus());
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/{id}/accounts")
    public ResponseEntity<List<AccountDTO>> accounts(@PathVariable String id) {
        String url = accountServiceBaseUrl + "/accounts?customerId=" + id;
        AccountDTO[] response = restTemplate.getForObject(url, AccountDTO[].class);
        List<AccountDTO> payload = response == null ? Collections.<AccountDTO>emptyList() : Arrays.asList(response);
        return ResponseEntity.ok(payload);
    }

    public static class CreateCustomerRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String creditCode;
        @NotBlank
        private String contactName;
        @NotBlank
        private String contactPhone;
        @NotBlank
        private String segment;
        private String riskLevel;
        private String status;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCreditCode() {
            return creditCode;
        }

        public void setCreditCode(String creditCode) {
            this.creditCode = creditCode;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }

        public String getSegment() {
            return segment;
        }

        public void setSegment(String segment) {
            this.segment = segment;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
