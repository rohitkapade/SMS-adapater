package com.tml.uep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.dto.product.api.Product;
import com.tml.uep.model.dto.product.ProductResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(AWSTestConfiguration.class)
public class ProductsControllerTest {

    @ClassRule
    public static WireMockRule productApiRule = new WireMockRule(7790);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

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
    public void shouldCallProductApiAndReturn400WhenMobileNumberIsNot10Digits() throws Exception {
        MvcResult mvcResult =
                mockMvc.perform(MockMvcRequestBuilders.get
                        ("/product/123"))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        verify(0, postRequestedFor(urlEqualTo(PRODUCT_API_ENDPOINT)));

        Assert.assertNotNull(mvcResult);
        Assert.assertNotNull(mvcResult.getResponse());
        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void shouldReturnProductListAndReturn200ResponseCodeWhenExternalApiCallIsSuccessful() throws Exception {
        URL productApiResponse = classLoader.getResource("mockResponses/productsResponse/productList.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(productApiResponse).toURI()));

        Product[] apiProductResponse = mapper.readValue(response, Product[].class);
        List<Product> apiProductDetailsList = Arrays.asList(apiProductResponse);

        ProductResponse actualResponse = getAllProducts(response, HttpStatus.OK.value(), status().isOk());

        verify(1, postRequestedFor(urlEqualTo(PRODUCT_API_ENDPOINT)));

        List<com.tml.uep.model.dto.product.Product> productDetailsList = actualResponse.getProducts();
        for (int i = 0; i < productDetailsList.size(); i++) {
            Assert.assertEquals(productDetailsList.get(i).getId(), apiProductDetailsList.get(i).getId());
            Assert.assertEquals(productDetailsList.get(i).getName(), apiProductDetailsList.get(i).getName());
        }
    }

    @Test
    public void shouldReturnEmptyProductListAndReturn200SuccessResponse() throws Exception {
        URL productApiResponse = classLoader.getResource("mockResponses/productsResponse/empty_response.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(productApiResponse).toURI()));

        ProductResponse actualResponse = getAllProducts(response, HttpStatus.OK.value(), status().isOk());

        verify(1, postRequestedFor(urlEqualTo(PRODUCT_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNotNull(actualResponse.getProducts());
        Assert.assertEquals(0, actualResponse.getProducts().size());
    }

    @Test
    public void shouldCallProductApiAndReturnEmptyListWhenExternalApiReturns412() throws Exception {
        URL productApiResponse = classLoader.getResource("mockResponses/productsResponse/error_response_412.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(productApiResponse).toURI()));

        ProductResponse actualResponse = getAllProducts(response, HttpStatus.PRECONDITION_FAILED.value(), status().isPreconditionFailed());
        verify(1, postRequestedFor(urlEqualTo(PRODUCT_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getProducts());
    }

    @Test
    public void shouldCallProductListApiAndWhenObjectMapperThrowsParsingException() throws Exception {
        URL productApiResponse = classLoader.getResource("mockResponses/productsResponse/parsing_error_500.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(productApiResponse).toURI()));

        ProductResponse actualResponse = getAllProducts(response, HttpStatus.OK.value(), status().isInternalServerError());
        verify(1, postRequestedFor(urlEqualTo(PRODUCT_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getProducts());
    }

    @Test
    public void shouldCallProductListApiAndReturnErrorStatusCodeWhenExternalApiThrowsJsonProcessingException() throws Exception {
        URL productApiResponse = classLoader.getResource("mockResponses/productsResponse/json_processing_exception.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(productApiResponse).toURI()));

        ProductResponse actualResponse = getAllProducts(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), status().isUnsupportedMediaType());
        verify(1, postRequestedFor(urlEqualTo(PRODUCT_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getProducts());
    }



    private ProductResponse getAllProducts(String apiResponse, int httpStatusCode, ResultMatcher resultMatcher) throws Exception {
        productApiRule.stubFor(
                WireMock.post(PRODUCT_API_ENDPOINT)
                        .withHeader("Content-Type", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withHeader("Accept", "application/json")
                                        .withStatus(httpStatusCode)
                                        .withBody(apiResponse)
                        )
        );

        MvcResult mvcResult =
                mockMvc.perform(MockMvcRequestBuilders.get
                        ("/product/1234567890"))
                        .andExpect(resultMatcher)
                        .andReturn();

        return mapper.readValue(mvcResult.getResponse().getContentAsString(), ProductResponse.class);
    }
}
