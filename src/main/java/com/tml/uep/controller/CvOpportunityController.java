package com.tml.uep.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.model.*;
import com.tml.uep.service.CvOpportunityService;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("opportunity-cv")
@Slf4j
public class CvOpportunityController {

    @Autowired private CvOpportunityService cvOpportunityService;

    @Autowired private ObjectMapper mapper;

    @PostMapping("")
    public ResponseEntity createCvOpportunity(
            @RequestBody CvOptyDetailsRequest cvOptyDetailsRequest) throws JsonProcessingException {
        log.info(
                "logging request for cv opty creation {} ",
                mapper.writeValueAsString(cvOptyDetailsRequest));
        CvOptyCreationResponse response =
                cvOpportunityService.createCvOpportunity(cvOptyDetailsRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/details")
    public ResponseEntity getCvOpportunityDetails(
            @Valid @RequestBody OpportunityDetailsRequest request) {
        log.info("Calling opportunity details API for opty ids: {} ", request.getOptyIds());
        int optyIdCount =
                request.getOptyIds().stream()
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList())
                        .size();
        if (optyIdCount <= 0 || optyIdCount > 10) {
            return ResponseEntity.badRequest().body("Opty id list cannot be empty or more than 10");
        }
        SolrOptyDetailsResponse response = cvOpportunityService.fetchOpportunityDetails(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity getOptyDetailList(
            @RequestParam String startDateTime, @RequestParam String endDateTime) {

        List<OptyDetailsResponse> optyDetailList =
                cvOpportunityService.getFilteredOptyIdList(startDateTime, endDateTime);
        if (optyDetailList == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(optyDetailList);
    }
}
