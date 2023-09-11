package com.tml.uep.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.ErrorResponse;
import com.tml.uep.model.dto.solr.city.CityListRequest;
import com.tml.uep.model.dto.solr.city.CityListResponse;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(AWSTestConfiguration.class)
public class CitySearchControllerTest {

    @ClassRule public static WireMockRule solarApiRule = new WireMockRule(8099);

    @Autowired private SolrApiWireMockUtils mockUtils;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper mapper;

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Before
    public void setup() {
        solarApiRule.resetAll();
    }

    @Test
    public void shouldCallGetCityListSOLRApiAndReturn200() throws Exception {

        URL fileUrlForCityListResponse =
                classLoader.getResource(
                        "mockResponses/cityListResponses/cityListSuccessResponse.json");
        String successResponse = Files.readString(Path.of(fileUrlForCityListResponse.toURI()));
        mockUtils.stubCityListSOLRApiResponse(successResponse, solarApiRule);
        CityListRequest request = new CityListRequest("MH");

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/city-search")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        CityListResponse cityListResponse =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), CityListResponse.class);

        Assert.assertNotNull(cityListResponse.getCityList());
        Assert.assertFalse(cityListResponse.getCityList().isEmpty());
        Assert.assertEquals("ABDALPUR", cityListResponse.getCityList().get(3));
        Assert.assertEquals("ABANPALLI", cityListResponse.getCityList().get(2));
        Assert.assertEquals("SOLAPUR", cityListResponse.getCityList().get(1));
        Assert.assertEquals("PUNE", cityListResponse.getCityList().get(0));

        mockUtils.verifyTimesCityListSolrApiWasCalled(1);
    }

    @Test
    public void shouldReturnBadRequestWhenStateIsMissingFromRequestBody() throws Exception {

        CityListRequest request = new CityListRequest("");
        MvcResult mvcResult =
                mockMvc.perform(
                                post("/city-search")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        ErrorResponse cityListResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals("400", cityListResponse.getStatus());
        Assert.assertEquals(List.of("state must not be blank"), cityListResponse.getErrors());

        mockUtils.verifyTimesCityListSolrApiWasCalled(0);
    }

    @Test
    public void shouldReturnInternalServerErrorWhenCityListSolrAPIReturnsInternalServerError()
            throws Exception {

        URL fileUrlForCityListResponse =
                classLoader.getResource(
                        "mockResponses/cityListResponses/cityListErrorResponse.json");
        String errorResponse = Files.readString(Path.of(fileUrlForCityListResponse.toURI()));
        mockUtils.stubCityListSOLRApiResponse(errorResponse, solarApiRule);
        CityListRequest request = new CityListRequest("MH");

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/city-search")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is5xxServerError())
                        .andReturn();

        Assert.assertEquals("Internal Server Error", mvcResult.getResponse().getContentAsString());
        Assert.assertEquals(
                "{\n"
                        + "  \"msg\": \"Unable to process the request.\",\n"
                        + "  \"error_code\": 30\n"
                        + "}",
                mvcResult.getResolvedException().getMessage());

        mockUtils.verifyTimesCityListSolrApiWasCalled(1);
    }
}
