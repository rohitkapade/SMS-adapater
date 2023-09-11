package com.tml.uep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.*;
import com.tml.uep.model.entity.CvOpportunity;
import com.tml.uep.repository.CvOpportunityRepository;
import com.tml.uep.utils.MaskingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CvOpportunityService {

    @Qualifier("optyCreationRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Autowired private ObjectMapper mapper;

    @Autowired private CvOpportunityRepository repository;

    @Autowired private TransliterationService transliterationService;

    @Value("${solr-api.opty-creation-endpoint}")
    private String CV_OPPTY_CREATION_END_POINT;

    @Value("${solr-api.opty-details-endpoint}")
    private String CV_OPPTY_DETAILS_END_POINT;

    private static final String SOLR_ERROR_STRING = "error_code";

    public CvOptyCreationResponse createCvOpportunity(CvOptyDetailsRequest request) {

        CvOptyCreationRequest cvOptyCreationRequest = getCvOptyCreationRequest(request);
        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(
                        CV_OPPTY_CREATION_END_POINT, cvOptyCreationRequest, String.class);
        String responseBody = responseEntity.getBody();
        HttpStatus statusCode = responseEntity.getStatusCode();

        if (!statusCode.is2xxSuccessful() || responseBody.contains(SOLR_ERROR_STRING)) {
            log.error(
                    "CV Opty Creation API returned status code: {} and body: {} for number: {}",
                    statusCode,
                    responseBody,
                    request.getMobileNumber());
            throw new ExternalSystemException(responseBody);
        }

        try {
            CvOptyCreationResponse response =
                    mapper.readValue(responseBody, CvOptyCreationResponse.class);
            saveCvOptyResponseDetails(cvOptyCreationRequest, response.getOpportunityId());
            return response;
        } catch (JsonProcessingException jpe) {
            log.error(
                    "Unable to parse create opportunity api response: {} for customer id: {} ",
                    responseBody,
                    MaskingUtils.maskMobileNumber(request.getMobileNumber()),
                    jpe);
            throw new ExternalSystemException(jpe.getMessage());
        }
    }

    private CvOptyCreationRequest getCvOptyCreationRequest(CvOptyDetailsRequest request) {
        CvOptyDetailsRequest detailsRequest = getCvOptyDetailsRequestAfterTransliteration(request);
        log.info("CV Opty creation request after transliteration {} ", detailsRequest);
        CvOptyCreationRequest cvOpportunityCreationRequest =
                new CvOptyCreationRequest(detailsRequest);
        log.info("Opty creation request sent to solr api {} ", cvOpportunityCreationRequest);
        return cvOpportunityCreationRequest;
    }

    public SolrOptyDetailsResponse fetchOpportunityDetails(OpportunityDetailsRequest optyIds) {

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(CV_OPPTY_DETAILS_END_POINT, optyIds, String.class);

        String responseBody = responseEntity.getBody();
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.error(
                    "CV Opty Details API returned status code: {} and body: {} for opty ids: {}",
                    responseEntity.getStatusCode(),
                    responseBody,
                    optyIds.getOptyIds());
            throw new ExternalSystemException(responseBody);
        }

        try {
            SolrOptyDetailsResponse response =
                    mapper.readValue(responseBody, SolrOptyDetailsResponse.class);
            return response;
        } catch (JsonProcessingException jpe) {
            log.error(
                    "Unable to parse solr opty details: {} for opty ids: {} ",
                    responseBody,
                    optyIds.getOptyIds(),
                    jpe);
            throw new ExternalSystemException(jpe.getMessage());
        }
    }

    private void saveCvOptyResponseDetails(CvOptyCreationRequest request, String optyId) {
        CvOpportunity cvOpportunity = new CvOpportunity(request, optyId);
        repository.save(cvOpportunity);
    }

    private CvOptyDetailsRequest getCvOptyDetailsRequestAfterTransliteration(
            CvOptyDetailsRequest cvOpportunityCreationDetailsRequest) {
        if (!"en".equals(cvOpportunityCreationDetailsRequest.getLanguageSelected())) {
            return transliterationService.transliterate(cvOpportunityCreationDetailsRequest);
        }
        return cvOpportunityCreationDetailsRequest;
    }

    public List<OptyDetailsResponse> getFilteredOptyIdList(
            String startDateTime, String endDateTime) {
        Optional<List<CvOpportunity>> optionalOptyList =
                repository.findAllByDateTimeBetween(
                        OffsetDateTime.parse(startDateTime), OffsetDateTime.parse(endDateTime));
        if (optionalOptyList.isEmpty()) {
            return null;
        }
        return optionalOptyList.get().stream()
                .map(OptyDetailsResponse::new)
                .filter(cvOpty -> StringUtils.isNotBlank(cvOpty.getOpportunityId()))
                .collect(Collectors.toList());
    }
}
