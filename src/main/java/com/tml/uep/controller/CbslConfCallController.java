package com.tml.uep.controller;

import com.tml.uep.model.CbslConfCallStatus;
import com.tml.uep.model.dto.cbslconfcall.CbslConfCallDetails;
import com.tml.uep.model.dto.cbslconfcall.ConfCallRecordingRequest;
import com.tml.uep.model.dto.cbslconfcall.ConfCallRequest;
import com.tml.uep.service.CbslConfCallService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("dealer-client-conf-call")
@Slf4j
public class CbslConfCallController {

    @Autowired private CbslConfCallService cbslConfCallService;

    @PostMapping("")
    public ResponseEntity createConfCallWithDealerAndCbslAgent(
            @RequestBody @Valid ConfCallRequest confCallRequest) {
        log.info("create cbsl conf call request : {} ", confCallRequest);
        String response = cbslConfCallService.initiateCall(confCallRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/call-records")
    public ResponseEntity updateConfCallDetailsWithRecordingUrl(
            @RequestBody @Valid ConfCallRecordingRequest request) {

        log.info("CBSL conf call details : {} ", request);
        cbslConfCallService.updateRecordingDetails(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/call-records")
    public ResponseEntity<List<CbslConfCallDetails>> getCbslConfCallRecordingDetails(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) CbslConfCallStatus status) {
        log.info(
                "Conf Call records details call with request params start date: {} , end date: {} and status: {}",
                startDate,
                endDate,
                status);
        return ResponseEntity.ok(
                cbslConfCallService.getCbslConfCallRecords(status, startDate, endDate));
    }
}
