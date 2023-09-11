package com.tml.uep.controller;

import com.tml.uep.model.OpportunityCreationRequest;
import com.tml.uep.service.OpportunityService;
import com.tml.uep.solr_api.dto.opportunity.OpportunitySolrResponse;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("opportunity")
@Slf4j
public class OpportunityController {

    @Autowired private OpportunityService opportunityService;

    @PostMapping("create")
    public ResponseEntity createOpportunity(
            @RequestBody OpportunityCreationRequest opportunityCreationRequest) {
        try {
            log.info("Request received: {}", opportunityCreationRequest.toString());
            Optional<OpportunitySolrResponse> opportunityResponse =
                    opportunityService.createOpportunity(opportunityCreationRequest);
            return opportunityResponse.isPresent()
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
