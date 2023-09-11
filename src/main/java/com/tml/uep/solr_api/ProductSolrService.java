package com.tml.uep.solr_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.solr_api.dto.ProductDetailRequest;
import com.tml.uep.solr_api.dto.ProductDetailResponse;
import com.tml.uep.solr_api.dto.ProductLineResponse;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ProductSolrService {
    @Autowired
    @Qualifier("solrRestTemplate")
    private RestTemplate restTemplate;

    @Value("${solr-api.product-detail-endpoint}")
    private String productDetailEndpoint;

    @Autowired private ObjectMapper mapper;

    public ProductDetailResponse getLobs() {
        ProductDetailRequest request = new ProductDetailRequest("lob", null, null);
        ResponseEntity<ProductDetailResponse> responseEntity =
                restTemplate.postForEntity(
                        productDetailEndpoint, request, ProductDetailResponse.class);

        if (responseEntity.getStatusCodeValue() != 200 || responseEntity.getBody() == null) {
            log.error(" Unable to fetch lob, Error occurred: {}", responseEntity.getBody());
            return new ProductDetailResponse(Collections.emptyList());
        }

        return responseEntity.getBody();
    }

    public ProductDetailResponse getPplForLob(String lob) {
        ProductDetailRequest request = new ProductDetailRequest("ppl", lob, null);
        ResponseEntity<ProductDetailResponse> responseEntity =
                restTemplate.postForEntity(
                        productDetailEndpoint, request, ProductDetailResponse.class);

        if (responseEntity.getStatusCodeValue() != 200 || responseEntity.getBody() == null) {
            log.error(
                    " Unable to fetch PPL for lob {}, Error occurred: {}",
                    lob,
                    responseEntity.getBody());
            return new ProductDetailResponse(Collections.emptyList());
        }
        return responseEntity.getBody();
    }

    public ProductLineResponse getPlForPplAndLob(String lob, String ppl) {
        ProductDetailRequest request = new ProductDetailRequest("pl", lob, ppl);
        ResponseEntity<ProductLineResponse> responseEntity =
                restTemplate.postForEntity(
                        productDetailEndpoint, request, ProductLineResponse.class);

        if (responseEntity.getStatusCodeValue() != 200 || responseEntity.getBody() == null) {
            log.error(
                    " Unable to fetch PL for lob {} and PPL {}, Error occurred: {}",
                    lob,
                    ppl,
                    responseEntity.getBody());
            return new ProductLineResponse(Collections.emptyList());
        }
        return responseEntity.getBody();
    }
}
