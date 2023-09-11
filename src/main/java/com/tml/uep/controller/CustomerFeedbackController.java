package com.tml.uep.controller;

import com.tml.uep.model.Group;
import com.tml.uep.model.dto.CustomerFeedback.CustomerFeedbackDto;
import com.tml.uep.model.dto.CustomerFeedback.CustomerFeedbackRequest;
import com.tml.uep.service.CustomerFeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("customer-feedback")
@Slf4j
public class CustomerFeedbackController {

    @Autowired private CustomerFeedbackService customerFeedbackService;

    @PostMapping()
    public ResponseEntity createCustomerFeedback(
            @Valid @RequestBody CustomerFeedbackRequest customerFeedbackRequest) {

        log.info("saving customer feedback : {}", customerFeedbackRequest);
        customerFeedbackService.saveCustomerFeedback(customerFeedbackRequest);

        return ResponseEntity.ok().build();
    }

    @GetMapping()
    public ResponseEntity getCustomerFeedback(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime startDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime endDateTime,
            @RequestParam Group groupName) {

        log.info(
                "Fetching customer feedback between dateTime: {} - {} and of group: {}",
                startDateTime,
                endDateTime,
                groupName.name());

        List<CustomerFeedbackDto> customerFeedbacks =
                customerFeedbackService.fetchCustomerFeedback(
                        startDateTime, endDateTime, groupName);

        return ResponseEntity.ok(customerFeedbacks);
    }
}
