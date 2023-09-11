package com.tml.uep.solr_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.solr_api.dto.opportunity.OpportunitySolrRequest;
import com.tml.uep.solr_api.dto.opportunity.OpportunitySolrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OpportunitySolrService {
    @Value("${solr-api.opty-creation-endpoint}")
    private String optyCreationEndpoint;

    @Autowired
    @Qualifier("solrRestTemplate")
    private RestTemplate restTemplate;

    @Autowired private ObjectMapper mapper;

    public OpportunitySolrResponse createOpportunityViaSOLRApi(
            OpportunitySolrRequest opportunitySolrRequest) throws JsonProcessingException {
        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(
                        optyCreationEndpoint, opportunitySolrRequest, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()
                && responseEntity.getBody().contains("id")) {
            log.info(
                    "The Opty Creation SOLR API returned success response: {}",
                    responseEntity.getBody());
            return mapper.readValue(responseEntity.getBody(), OpportunitySolrResponse.class);
        } else {
            log.error(
                    "The Opty Creation SOLR API returned status code: {} and body: {}",
                    responseEntity.getStatusCode(),
                    responseEntity.getBody());
            throw new ExternalSystemException(responseEntity.getBody());
        }
    }
}
