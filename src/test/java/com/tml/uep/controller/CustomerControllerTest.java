package com.tml.uep.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.CustomerDetailsResponse;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(AWSTestConfiguration.class)
public class CustomerControllerTest {

    @ClassRule public static WireMockRule solarApiRule = new WireMockRule(8099);

    @Autowired private SolrApiWireMockUtils mockUtils;

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper mapper;

    @Before
    public void setUp() {
        solarApiRule.resetRequests();
    }

    @Test
    public void shouldGetCustomerDetailsFromSolr() throws Exception {
        URL url = classLoader.getResource("mockResponses/customerDetails/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubCustomerSolarApiResponse(apiResponse, solarApiRule);

        MvcResult mvcResult =
                mockMvc.perform(get("/customer/details/919908523366"))
                        .andExpect(status().isOk())
                        .andReturn();

        mockUtils.verifyTimesCustomerSolrApiWasCalled(1);

        CustomerDetailsResponse customerDetailsResponse =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        Assert.assertEquals("KURNOOL", customerDetailsResponse.getCity());
        Assert.assertEquals("", customerDetailsResponse.getEmail());
        Assert.assertEquals("Andhra Pradesh", customerDetailsResponse.getState());
        Assert.assertEquals("", customerDetailsResponse.getPincode());
        Assert.assertEquals("FAROOQ S.", customerDetailsResponse.getName());
    }

    @Test
    public void shouldGiveNotFoundWhenCustomerDetailsNotPresentInSolr() throws Exception {
        mockUtils.stubCustomerSolarApiResponse("[]", solarApiRule);

        mockMvc.perform(get("/customer/details/919908523366"))
                .andExpect(status().isNotFound())
                .andReturn();

        mockUtils.verifyTimesCustomerSolrApiWasCalled(1);
    }

    @Test
    public void shouldGiveNotFoundWhenSolrGivesErrorCodeForCustomerDetails() throws Exception {
        mockUtils.stubCustomerSolarApiErrorResponse(solarApiRule);

        mockMvc.perform(get("/customer/details/919908523366"))
                .andExpect(status().isNotFound())
                .andReturn();

        mockUtils.verifyTimesCustomerSolrApiWasCalled(1);
    }
}
