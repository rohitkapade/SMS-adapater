package com.tml.uep.solr_api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.solr_api.dto.CustomerSolrRequest;
import com.tml.uep.solr_api.dto.CustomerSolrResponse;
import com.tml.uep.utils.MaskingUtils;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CustomerSolrService {

    @Autowired
    @Qualifier("solrRestTemplate")
    private RestTemplate restTemplate;

    @Value("${solr-api.customer-fetch-url}")
    private String customerFetchUrl;

    @Autowired private ObjectMapper mapper;

    public List<CustomerSolrResponse> getCustomerDetails(String phoneNumber) {
        try {
            String numberWithoutCode =
                    phoneNumber.length() > 10
                            ? phoneNumber.substring(phoneNumber.length() - 10)
                            : phoneNumber;
            CustomerSolrRequest customerSolrRequest = new CustomerSolrRequest(numberWithoutCode);

            ResponseEntity<String> responseEntity =
                    restTemplate.postForEntity(customerFetchUrl, customerSolrRequest, String.class);

            if (responseEntity.getStatusCodeValue() != 200 || responseEntity.getBody() == null) {
                log.error(
                        " Unable to fetch information for customer {} from SOLR {} {}",
                        MaskingUtils.maskMobileNumber(phoneNumber),
                        responseEntity.getStatusCode(),
                        responseEntity.getBody());
                return Collections.emptyList();
            }
            return mapper.readValue(responseEntity.getBody(), new TypeReference<>() {});
        } catch (Exception e) {
            log.error(
                    "Unable to fetch information for customer {} from SOLR",
                    MaskingUtils.maskMobileNumber(phoneNumber),
                    e);
            return Collections.emptyList();
        }
    }
}
