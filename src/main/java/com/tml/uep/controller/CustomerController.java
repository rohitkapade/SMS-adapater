package com.tml.uep.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tml.uep.model.CustomerDetailsResponse;
import com.tml.uep.service.CustomerService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("customer")
@Slf4j
public class CustomerController {

    @Autowired private CustomerService customerService;

    @GetMapping("details/{phoneNumber}")
    public ResponseEntity<CustomerDetailsResponse> getCustomerDetails(
            @PathVariable("phoneNumber") String phoneNumber) throws JsonProcessingException {
        Optional<CustomerDetailsResponse> customerDetails =
                customerService.getCustomerDetails(phoneNumber);
        return customerDetails
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
