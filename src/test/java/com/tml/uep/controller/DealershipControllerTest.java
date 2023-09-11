package com.tml.uep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.dto.dealership.api.Dealership;
import com.tml.uep.model.dto.dealership.DealershipResponse;
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
public class DealershipControllerTest {

    @ClassRule
    public static WireMockRule dealershipApiRule = new WireMockRule(7789);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Value("${dealership-api.dealership-endpoint}")
    private String DEALERSHIP_API_ENDPOINT;

    @Before
    public void setup() {
        dealershipApiRule.resetAll();
    }

    @Test
    public void shouldCallDealershipApiAndReturn400WhenMobileNumberIsNot10Digits() throws Exception {
        MvcResult mvcResult =
                mockMvc.perform(MockMvcRequestBuilders.get
                                ("/dealership/123"))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        verify(0, postRequestedFor(urlEqualTo(DEALERSHIP_API_ENDPOINT)));

        Assert.assertNotNull(mvcResult);
        Assert.assertNotNull(mvcResult.getResponse());
        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void shouldCallDealershipApiAndReturn200StatusCodeWhenExternalApiCallSucceeds() throws Exception {
        URL dealershipApiResponse = classLoader.getResource("mockResponses/dealershipResponse/dealersList.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(dealershipApiResponse).toURI()));

        Dealership[] apiDealershipResponse = mapper.readValue(response, Dealership[].class);
        List<Dealership> apiDealershipResponseList = Arrays.asList(apiDealershipResponse);

        DealershipResponse actualResponse = getAllDealers(response, HttpStatus.OK.value(), status().isOk());

        verify(1, postRequestedFor(urlEqualTo(DEALERSHIP_API_ENDPOINT)));

        List<com.tml.uep.model.dto.dealership.Dealership> dealerDetailsList = actualResponse.getDealerships();
        for (int i = 0; i < dealerDetailsList.size(); i++) {
            Assert.assertEquals(dealerDetailsList.get(i).getId(), apiDealershipResponseList.get(i).getDivId());
            Assert.assertEquals(dealerDetailsList.get(i).getName(), apiDealershipResponseList.get(i).getOrgName());
            Assert.assertEquals(dealerDetailsList.get(i).getAddress(), apiDealershipResponseList.get(i).getOrgAddress());
        }
    }

    @Test
    public void shouldCallDealershipApiAndReturn200SuccessWithEmptyDealershipList() throws Exception {
        URL dealershipApiResponse = classLoader.getResource("mockResponses/dealershipResponse/empty_list_response.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(dealershipApiResponse).toURI()));

        DealershipResponse actualResponse = getAllDealers(response, HttpStatus.OK.value(), status().isOk());

        verify(1, postRequestedFor(urlEqualTo(DEALERSHIP_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNotNull(actualResponse.getDealerships());
        Assert.assertEquals(0, actualResponse.getDealerships().size());
    }

    @Test
    public void shouldCallDealershipApiAndReturnEmptyListWhenExternalApiReturns412() throws Exception {
        URL dealershipApiResponse = classLoader.getResource("mockResponses/dealershipResponse/error_response_412.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(dealershipApiResponse).toURI()));

        DealershipResponse actualResponse = getAllDealers(response, HttpStatus.PRECONDITION_FAILED.value(), status().isPreconditionFailed());
        verify(1, postRequestedFor(urlEqualTo(DEALERSHIP_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getDealerships());
    }

    @Test
    public void shouldCallDealershipApiAndReturnEmptyListWhenExternalApiThrowsJsonProcessingException() throws Exception {
        URL dealershipApiResponse = classLoader.getResource("mockResponses/dealershipResponse/json_processing_exception.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(dealershipApiResponse).toURI()));

        DealershipResponse actualResponse = getAllDealers(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), status().isUnsupportedMediaType());
        verify(1, postRequestedFor(urlEqualTo(DEALERSHIP_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getDealerships());
    }

    @Test
    public void shouldCallDealershipApiAndReturnEmptyListWhenObjectMapperThrowsParsingException() throws Exception {
        URL dealershipApiResponse = classLoader.getResource("mockResponses/dealershipResponse/parsing_error_500.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(dealershipApiResponse).toURI()));

        DealershipResponse actualResponse = getAllDealers(response, HttpStatus.OK.value(), status().isInternalServerError());
        verify(1, postRequestedFor(urlEqualTo(DEALERSHIP_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getDealerships());
    }

    private DealershipResponse getAllDealers(String apiResponse, int httpStatusCode, ResultMatcher resultMatcher) throws Exception {
        dealershipApiRule.stubFor(
                WireMock.post(DEALERSHIP_API_ENDPOINT)
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
                        ("/dealership/1234567890"))
                        .andExpect(resultMatcher)
                        .andReturn();

        return mapper.readValue(mvcResult.getResponse().getContentAsString(), DealershipResponse.class);
    }
}