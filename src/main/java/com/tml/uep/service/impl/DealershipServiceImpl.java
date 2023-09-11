package com.tml.uep.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.dto.dealership.api.Dealership;
import com.tml.uep.model.dto.dealership.api.DealershipRequest;
import com.tml.uep.model.dto.dealership.api.DealershipResponse;
import com.tml.uep.service.DealershipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DealershipServiceImpl implements DealershipService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${dealership-api.base-url}")
    private String BASE_URL;

    @Value("${dealership-api.dealership-endpoint}")
    private String DEALERSHIP_API_ENDPOINT;

    @Override
    public com.tml.uep.model.dto.dealership.DealershipResponse getAllDealers(String mobileNumber) {
        HttpHeaders httpHeaders = getHeaders();
        DealershipRequest request = new DealershipRequest(mobileNumber);
        DealershipResponse apiResponse = invokeDealershipApi(httpHeaders, request);
        if (apiResponse.getHttpStatus() == HttpStatus.OK) {
            List<com.tml.uep.model.dto.dealership.Dealership> dealerDetailsList = apiResponse.getDealers()
                    .stream()
                    .map(dealership -> new com.tml.uep.model.dto.dealership.Dealership(dealership.getDivId(), dealership.getOrgName(), dealership.getOrgAddress()))
                    .collect(Collectors.toList());
            return new com.tml.uep.model.dto.dealership.DealershipResponse(dealerDetailsList, apiResponse.getHttpStatus());
        }
        return new com.tml.uep.model.dto.dealership.DealershipResponse(null, apiResponse.getHttpStatus());
    }

    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }

    private DealershipResponse invokeDealershipApi(HttpHeaders httpHeaders, DealershipRequest request) {
        String responseBody;
        HttpStatus httpStatus;
        List<Dealership> dealerDetailsList = null;

        try {
            HttpEntity<DealershipRequest> httpEntity = new HttpEntity<>(request, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    BASE_URL.concat(DEALERSHIP_API_ENDPOINT),
                    httpEntity,
                    String.class);

            responseBody = responseEntity.getBody();
            httpStatus = responseEntity.getStatusCode();
            log.info("Dealership API response: {}", responseBody);

            Dealership[] apiResponseArray = objectMapper.readValue(responseEntity.getBody(), Dealership[].class);
            dealerDetailsList = Arrays.asList(apiResponseArray);
        } catch (ExternalSystemException ex) {
            httpStatus = ex.getHttpStatus();
            log.error("External exception occurred ", ex);
        } catch (JsonProcessingException ex) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error("Error occurred while calling dealership api ", ex);
        }
        return new DealershipResponse(dealerDetailsList, httpStatus);
    }
}