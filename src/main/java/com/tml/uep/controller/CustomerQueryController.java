package com.tml.uep.controller;

import com.tml.uep.model.ErrorResponse;
import com.tml.uep.model.dto.customerquery.CustomerQueryDTO;
import com.tml.uep.model.dto.customerquery.CustomerQueryStatus;
import com.tml.uep.model.dto.customerquery.CustomerQueryUpdateRequest;
import com.tml.uep.service.CustomerQueryService;
import javax.validation.Valid;

import com.tml.uep.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("customer-query")
@Slf4j
public class CustomerQueryController {

    @Autowired private CustomerQueryService customerQueryService;

    @PutMapping("/{queryId}")
    public ResponseEntity<Object> updateCustomerQuery(@PathVariable long queryId, @RequestBody @Valid CustomerQueryUpdateRequest updateRequest) {
        log.info("Incoming CustomerQueryUpdateRequest for queryId {} : {}", queryId, updateRequest);
        boolean isUpdateSuccessful = customerQueryService.updateCustomerQuery(queryId, updateRequest);
        if(!isUpdateSuccessful) {
            log.error("{} {}", Constants.INVALID_QUERY_ID_MESSAGE, queryId);
            return getBadRequestResponseEntity(Constants.INVALID_QUERY_ID_MESSAGE);
        }

        return ResponseEntity.ok().build();

    }

    private static ResponseEntity<Object> getBadRequestResponseEntity(String errorMessage) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(OffsetDateTime.now(ZoneOffset.UTC).toString(),
                        String.format("%s",HttpStatus.BAD_REQUEST.value()),
                        List.of(errorMessage))
        );
    }

    @GetMapping("")
    public ResponseEntity<List<CustomerQueryDTO>> getCustomerQueries(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDateTime,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDateTime,
                                                                     @RequestParam(required = false) CustomerQueryStatus status,
                                                                     @RequestParam(required = false) String assignedTo) {
        log.info("Fetching customer query records for date time range {} - {}, status {}, assignedTo {}",
                fromDateTime, toDateTime, status, assignedTo);
        List<CustomerQueryDTO> queries = customerQueryService.getCustomerQueries(fromDateTime, toDateTime, status, assignedTo);
        return ResponseEntity.ok().body(queries);
    }
}
