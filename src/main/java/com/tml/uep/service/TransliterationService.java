package com.tml.uep.service;

import static org.eclipse.jetty.http.HttpHeader.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.ContentLanguageCode;
import com.tml.uep.model.CvOptyDetailsRequest;
import com.tml.uep.model.TransliterateRequest;
import com.tml.uep.model.TransliterateResponse;
import com.tml.uep.utils.MaskingUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class TransliterationService {

    @Value("${transliterate-api.rev-app-name}")
    private String REV_APP_NAME;

    @Value("${transliterate-api.rev-api-key}")
    private String REV_API_KEY;

    @Value("${transliterate-api.rev-app-id}")
    private String REV_APP_ID;

    @Value("${transliterate-api.transliterate-url}")
    private String TRANSLITERATE_URL;

    @Value("${transliterate-api.transliterate-domain}")
    private String TRANSLITERATE_DOMAIN;

    @Autowired private RestTemplate restTemplate;

    public CvOptyDetailsRequest transliterate(
            CvOptyDetailsRequest cvOpportunityCreationDetailsRequest) {
        try {
            HttpHeaders headers =
                    getHttpHeadersForTransliterationAPI(cvOpportunityCreationDetailsRequest);
            TransliterateRequest request =
                    new TransliterateRequest(cvOpportunityCreationDetailsRequest);
            HttpEntity<TransliterateRequest> entity = new HttpEntity<>(request, headers);
            log.info("Transliteration API request : {} ", request);

            ResponseEntity<TransliterateResponse> responseEntity =
                    restTemplate.postForEntity(
                            TRANSLITERATE_URL, entity, TransliterateResponse.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()
                    && responseEntity.getBody() != null
                    && !CollectionUtils.isEmpty(responseEntity.getBody().getResponseList())) {
                log.info(
                        "The Transliteration API returned success response: {}",
                        responseEntity.getBody());
                return new CvOptyDetailsRequest(
                        responseEntity.getBody().getResponseList(),
                        cvOpportunityCreationDetailsRequest);
            } else {
                log.error(
                        "The Transliteration API returned status code: {} and body: {}",
                        responseEntity.getStatusCode(),
                        responseEntity.getBody());
                throw new ExternalSystemException(responseEntity.getBody().toString());
            }

        } catch (Exception e) {
            log.error(
                    "Unable to call transliteration API",
                    MaskingUtils.maskMobileNumber(
                            cvOpportunityCreationDetailsRequest.getMobileNumber()),
                    e);
            throw new ExternalSystemException(e.getMessage());
        }
    }

    private HttpHeaders getHttpHeadersForTransliterationAPI(
            CvOptyDetailsRequest cvOpportunityCreationDetailsRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.CONNECTION, List.of("close"));
        headers.put(CONTENT_TYPE.asString(), List.of(APPLICATION_JSON.toString()));
        headers.put("REV-API-KEY", List.of(REV_API_KEY));
        headers.put("REV-APP-ID", List.of(REV_APP_ID));
        headers.put("src_lang", List.of(cvOpportunityCreationDetailsRequest.getLanguageSelected()));
        headers.put("tgt_lang", List.of(ContentLanguageCode.en.toString()));
        headers.put("domain", List.of(TRANSLITERATE_DOMAIN));
        headers.put("cnt_lang", List.of(ContentLanguageCode.en.toString()));
        headers.put("REV-APPNAME", List.of(REV_APP_NAME));
        return headers;
    }
}
