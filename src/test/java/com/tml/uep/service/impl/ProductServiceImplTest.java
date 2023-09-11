package com.tml.uep.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.dto.product.ProductResponse;
import com.tml.uep.model.dto.product.api.Product;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({AWSTestConfiguration.class})
@RunWith(SpringRunner.class)
public class ProductServiceImplTest {

    @ClassRule
    public static WireMockRule productApiRule = new WireMockRule(7790);

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private ObjectMapper mapper;

    @Value("${product-api.product-endpoint}")
    private String PRODUCT_API_ENDPOINT;

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Before
    public void setup() {
        productApiRule.resetAll();
    }

    @Test
    public void shouldCallProductAndReturn2xxSuccessWhenDataReturned() throws Exception {
        URL productApiResponse = classLoader.getResource("mockResponses/productsResponse/productList.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(productApiResponse).toURI()));

        Product[] apiProductResponse = mapper.readValue(response, Product[].class);
        List<Product> apiProductDetailsList = Arrays.asList(apiProductResponse);

        ProductResponse actualResponse = getAllProducts(response, HttpStatus.OK.value());

        verify(1, postRequestedFor(urlEqualTo(PRODUCT_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertEquals(apiProductDetailsList.size(), actualResponse.getProducts().size());

        List<com.tml.uep.model.dto.product.Product> productDetailsList = actualResponse.getProducts();
        for (int i = 0; i < productDetailsList.size(); i++) {
            Assert.assertEquals(productDetailsList.get(i).getId(), apiProductDetailsList.get(i).getId());
            Assert.assertEquals(productDetailsList.get(i).getName(), apiProductDetailsList.get(i).getName());
        }
    }

    @Test
    public void shouldCallProductAndApiResponseThrowParseException() throws Exception {
        URL productApiResponse = classLoader.getResource("mockResponses/productsResponse/error_response_412.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(productApiResponse).toURI()));

        ProductResponse actualResponse = getAllProducts(response, HttpStatus.PRECONDITION_FAILED.value());

        verify(1, postRequestedFor(urlEqualTo(PRODUCT_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getProducts());
    }

    @Test
    public void shouldCallProductAndWhenObjectMapperThrowsParsingException() throws Exception {
        URL productApiResponse = classLoader.getResource("mockResponses/productsResponse/parsing_error_500.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(productApiResponse).toURI()));

        ProductResponse actualResponse = getAllProducts(response, HttpStatus.OK.value());

        verify(1, postRequestedFor(urlEqualTo(PRODUCT_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getProducts());
    }

    @Test
    public void shouldCallProductListApiAndReturnErrorStatusCodeWhenExternalApiThrowsJsonProcessingException() throws Exception {
        URL productApiResponse = classLoader.getResource("mockResponses/productsResponse/json_processing_exception.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(productApiResponse).toURI()));

        ProductResponse actualResponse = getAllProducts(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        verify(1, postRequestedFor(urlEqualTo(PRODUCT_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getProducts());
    }


    private ProductResponse getAllProducts(String apiResponse, int httpStatusCode) {
        productApiRule.stubFor(
                WireMock.post("/api/get/recent_products/")
                        .withHeader("Content-Type", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withHeader("Accept", "application/json")
                                        .withStatus(httpStatusCode)
                                        .withBody(apiResponse)
                        )
        );

        return productService.getAllProducts("1234567890");
    }
}