package com.tml.uep.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.dto.product.api.Product;
import com.tml.uep.model.dto.product.api.ProductRequest;
import com.tml.uep.model.dto.product.api.ProductResponse;
import com.tml.uep.service.ProductsService;
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
public class ProductServiceImpl implements ProductsService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${product-api.base-url}")
    private String BASE_URL;

    @Value("${product-api.product-endpoint}")
    private String PRODUCT_API_ENDPOINT;

    @Override
    public com.tml.uep.model.dto.product.ProductResponse getAllProducts(String mobileNumber) {
        HttpHeaders httpHeaders = getHeaders();
        ProductRequest request = new ProductRequest(mobileNumber);
        ProductResponse apiResponse = invokeProductApi(httpHeaders, request);
        if (apiResponse.getHttpStatus() == HttpStatus.OK) {
            List<com.tml.uep.model.dto.product.Product> productDetailsList = apiResponse.getProducts()
                    .stream()
                    .map(product -> new com.tml.uep.model.dto.product.Product(product.getId(), product.getName()))
                    .collect(Collectors.toList());
            return new com.tml.uep.model.dto.product.ProductResponse(productDetailsList, apiResponse.getHttpStatus());
        }
        return new com.tml.uep.model.dto.product.ProductResponse(null, apiResponse.getHttpStatus());
    }

    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }

    private ProductResponse invokeProductApi(HttpHeaders httpHeaders, ProductRequest request) {
        String responseBody;
        HttpStatus httpStatus;
        List<Product> productList = null;

        try {
            HttpEntity<ProductRequest> httpEntity = new HttpEntity<>(request, httpHeaders);
            ResponseEntity<String> productApiResponseEntity = restTemplate.postForEntity(
                    BASE_URL.concat(PRODUCT_API_ENDPOINT),
                    httpEntity,
                    String.class);

            responseBody = productApiResponseEntity.getBody();
            httpStatus = productApiResponseEntity.getStatusCode();
            log.info("Product API response: {}", responseBody);

            Product[] apiResponseArray = objectMapper.readValue(productApiResponseEntity.getBody(), Product[].class);
            productList = Arrays.asList(apiResponseArray);
        } catch (ExternalSystemException ex) {
            httpStatus = ex.getHttpStatus();
            log.error("External exception occurred ", ex);
        } catch (JsonProcessingException ex) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error("Error occurred while calling dealership api ", ex);
        }
        return new ProductResponse(productList, httpStatus);
    }
}