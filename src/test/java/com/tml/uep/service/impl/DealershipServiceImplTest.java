package com.tml.uep.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.dto.dealership.DealershipResponse;
import com.tml.uep.model.dto.dealership.api.Dealership;
import com.tml.uep.service.DealershipService;
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


@SpringBootTest
@Import({AWSTestConfiguration.class})
@RunWith(SpringRunner.class)
public class DealershipServiceImplTest {

    @ClassRule
    public static WireMockRule dealershipApiRule = new WireMockRule(7789);

    @Autowired
    private DealershipService dealershipService;

    @Autowired
    private ObjectMapper mapper;

    @Value("${dealership-api.dealership-endpoint}")
    private String DEALERSHIP_API_ENDPOINT;

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Before
    public void setup() {
        dealershipApiRule.resetAll();
    }

    @Test
    public void shouldCallDealershipAndReturn2xxSuccessWhenDataReturned() throws Exception {
        URL dealershipApiResponse = classLoader.getResource("mockResponses/dealershipResponse/dealersList.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(dealershipApiResponse).toURI()));

        Dealership[] apiDealershipResponse = mapper.readValue(response, Dealership[].class);
        List<Dealership> apiDealershipResponseList = Arrays.asList(apiDealershipResponse);

        DealershipResponse actualResponse = getAllDealers(response, HttpStatus.OK.value());

        verify(1, postRequestedFor(urlEqualTo(DEALERSHIP_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertEquals(apiDealershipResponseList.size(), actualResponse.getDealerships().size());

        List<com.tml.uep.model.dto.dealership.Dealership> dealerDetailsList = actualResponse.getDealerships();
        for (int i = 0; i < dealerDetailsList.size(); i++) {
            Assert.assertEquals(dealerDetailsList.get(i).getId(), apiDealershipResponseList.get(i).getDivId());
            Assert.assertEquals(dealerDetailsList.get(i).getName(), apiDealershipResponseList.get(i).getOrgName());
            Assert.assertEquals(dealerDetailsList.get(i).getAddress(), apiDealershipResponseList.get(i).getOrgAddress());
        }
    }

    @Test
    public void shouldCallDealershipAndApiResponseThrowParseException() throws Exception {
        URL dealershipApiResponse = classLoader.getResource("mockResponses/dealershipResponse/error_response_412.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(dealershipApiResponse).toURI()));

        DealershipResponse actualResponse = getAllDealers(response, HttpStatus.PRECONDITION_FAILED.value());

        verify(1, postRequestedFor(urlEqualTo(DEALERSHIP_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getDealerships());
    }

    @Test
    public void shouldCallDealershipAndWhenObjectMapperThrowsParsingException() throws Exception {
        URL dealershipApiResponse = classLoader.getResource("mockResponses/dealershipResponse/parsing_error_500.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(dealershipApiResponse).toURI()));

        DealershipResponse actualResponse = getAllDealers(response, HttpStatus.OK.value());

        verify(1, postRequestedFor(urlEqualTo(DEALERSHIP_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getDealerships());
    }

    @Test
    public void shouldCallDealershipApiAndReturnEmptyListWhenExternalApiThrowsJsonProcessingException() throws Exception {
        URL dealershipApiResponse = classLoader.getResource("mockResponses/dealershipResponse/json_processing_exception.json");
        String response = Files.readString(Path.of(Objects.requireNonNull(dealershipApiResponse).toURI()));

        DealershipResponse actualResponse = getAllDealers(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        verify(1, postRequestedFor(urlEqualTo(DEALERSHIP_API_ENDPOINT)));

        Assert.assertNotNull(actualResponse);
        Assert.assertNull(actualResponse.getDealerships());
    }

    private DealershipResponse getAllDealers(String apiResponse, int httpStatusCode) {
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

        return dealershipService.getAllDealers("1234567890");
    }



}