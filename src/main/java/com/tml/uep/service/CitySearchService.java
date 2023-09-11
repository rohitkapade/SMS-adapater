package com.tml.uep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.dto.solr.city.CityListRequest;
import com.tml.uep.model.dto.solr.city.CityListResponse;
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
public class CitySearchService {

    @Qualifier("optyCreationRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Autowired private ObjectMapper mapper;

    @Value("${solr-api.city-list-endpoint}")
    private String CITY_LIST_ENDPOINT;

    private final String SOLR_ERROR_STRING = "error_code";

    public CityListResponse fetchCityList(CityListRequest stateDetails) {
        String state = stateDetails.getState();

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(CITY_LIST_ENDPOINT, stateDetails, String.class);
        String responseBody = responseEntity.getBody();
        HttpStatus statusCode = responseEntity.getStatusCode();

        if (!statusCode.is2xxSuccessful() || responseBody.contains(SOLR_ERROR_STRING)) {
            log.error(
                    "The city list API for state: {} returned status code: {} and body: {}",
                    state,
                    statusCode,
                    responseBody);
            throw new ExternalSystemException(responseBody);
        }
        try {
            CityListResponse response = mapper.readValue(responseBody, CityListResponse.class);
            return response;
        } catch (JsonProcessingException jpe) {
            throw new ExternalSystemException("Unable to parse city list response ", jpe);
        }
    }
}
