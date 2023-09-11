package com.tml.uep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.dto.solr.division.DivisionListRequest;
import com.tml.uep.model.dto.solr.division.DivisionListResponse;
import com.tml.uep.model.dto.solr.division.SolrErrorResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class DivisionService {

    @Qualifier("optyCreationRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Autowired private ObjectMapper mapper;

    @Value("${solr-api.empty-division-list-status-codes}")
    private List<String> emptyDivisionListStatusCodes;

    @Value("${solr-api.division-list-endpoint}")
    private String DIVISION_LIST_END_POINT;

    private final String SOLR_ERROR_STRING = "error_code";

    public DivisionListResponse fetchDivisionList(DivisionListRequest stateCityDetails) {
        String state = stateCityDetails.getState();
        String city = stateCityDetails.getCity();
        String errorMessage =
                "The division list API for state: {} and city : {} returned status code: {} and body: {}";
        try {
            ResponseEntity<String> responseEntity =
                    restTemplate.postForEntity(
                            DIVISION_LIST_END_POINT, stateCityDetails, String.class);
            String responseBody = responseEntity.getBody();
            HttpStatus statusCode = responseEntity.getStatusCode();

            if (!statusCode.is2xxSuccessful()) {
                log.error(errorMessage, state, city, statusCode, responseBody);
                throw new ExternalSystemException(responseBody);
            }

            if (!responseBody.contains(SOLR_ERROR_STRING)) {
                DivisionListResponse response =
                        mapper.readValue(responseBody, DivisionListResponse.class);
                return response;
            }

            if (isDivisionListEmpty(responseBody)) {
                return new DivisionListResponse(List.of());
            }

            log.error(errorMessage, state, city, statusCode, responseBody);
            throw new ExternalSystemException(responseBody);

        } catch (JsonProcessingException jpe) {
            throw new ExternalSystemException("Unable to parse division list ", jpe);
        }
    }

    private boolean isDivisionListEmpty(String responseBody) throws JsonProcessingException {
        SolrErrorResponse response = mapper.readValue(responseBody, SolrErrorResponse.class);
        if (emptyDivisionListStatusCodes.contains(response.getErrorCode())) {
            return true;
        }
        return false;
    }
}
