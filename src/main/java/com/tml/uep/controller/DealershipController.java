package com.tml.uep.controller;

import com.tml.uep.model.dto.dealership.DealershipResponse;
import com.tml.uep.service.DealershipService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Size;

@Slf4j
@RestController
@Validated
@RequestMapping("/dealership")
@AllArgsConstructor
public class DealershipController {

    private final DealershipService dealershipService;

    @GetMapping("/{mobileNumber}")
    public ResponseEntity<DealershipResponse> getAllDealers(@PathVariable @Size(min = 10, max = 10, message = "Mobile number cannot be of less than 10 digits") String mobileNumber) {

        log.info("Incoming request to dealership list endpoint for {}", mobileNumber);

        DealershipResponse dealershipResponse = dealershipService.getAllDealers(mobileNumber);

        return ResponseEntity.status(dealershipResponse.getHttpStatus()).body(dealershipResponse);
    }
}