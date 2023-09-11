package com.tml.uep.controller;

import com.tml.uep.model.CallbackRequestStatus;
import com.tml.uep.model.dto.callbackrequest.CallbackUpdateRequest;
import com.tml.uep.service.CallbackRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("callback-request")
@Slf4j
public class CallbackRequestController {

    @Autowired private CallbackRequestService callbackRequestService;


    @PutMapping("/{callbackId}")
    public ResponseEntity updateCallbackRequest(
            @RequestBody @Valid CallbackUpdateRequest callbackUpdateRequest,
            @PathVariable Long callbackId) {

        log.info("update callback request received for callback id : {}", callbackId);
        boolean isUpdateSuccessful =
                callbackRequestService.updateCallbackRequest(callbackUpdateRequest, callbackId);
        if (isUpdateSuccessful) {
            return ResponseEntity.ok().build();
        }
        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "CallbackId not available");
    }

    @GetMapping()
    public ResponseEntity<List> getCallbackRequests(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime startDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime endDateTime,
            @RequestParam(required = false) CallbackRequestStatus status,
            @RequestParam(required = false) String assignedTo) {
        log.info("Fetching callback requests for date time range {} - {}, status {}, assignedTo {}",
                startDateTime, endDateTime, status, assignedTo);
        return ResponseEntity.ok(
                callbackRequestService.getCallbackRequests(startDateTime, endDateTime, status, assignedTo));
    }
}
