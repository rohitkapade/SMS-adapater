package com.tml.uep.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.ErrorResponse;
import com.tml.uep.model.dto.solr.division.DivisionDetails;
import com.tml.uep.model.dto.solr.division.DivisionListRequest;
import com.tml.uep.model.dto.solr.division.DivisionListResponse;
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
public class DivisionControllerTest {

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
    public void shouldCallGetDivisionListSOLRApiAndReturn200() throws Exception {

        URL fileUrlForDivisionListResponse =
                classLoader.getResource(
                        "mockResponses/divisionListResponses/divisionListSuccessResponse.json");
        String successResponse = Files.readString(Path.of(fileUrlForDivisionListResponse.toURI()));

        mockUtils.stubDivisionListSOLRApiResponse(successResponse, solarApiRule);
        DivisionListRequest request = new DivisionListRequest("MH", "PUNE");

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/division-search")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        DivisionListResponse divisionListResponse =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), DivisionListResponse.class);

        Assert.assertEquals(2, divisionListResponse.getDivisionList().size());

        DivisionDetails firstDivision = divisionListResponse.getDivisionList().get(0);
        DivisionDetails secondDivision = divisionListResponse.getDivisionList().get(1);

        Assert.assertEquals("BAFNA AUTO ENGINEERING PVT. LTD.", firstDivision.getOrgName());
        Assert.assertEquals("1-EE7V5", firstDivision.getDivisionId());
        Assert.assertEquals("1001700-Sales-Katraj-BafAuE", firstDivision.getDivisionName());
        Assert.assertEquals("1001700", firstDivision.getDealerCode());
        Assert.assertEquals("PUNE", firstDivision.getTaluka());
        Assert.assertEquals("PUNE", firstDivision.getCity());
        Assert.assertEquals("PUNE", firstDivision.getDistrict());
        Assert.assertEquals("MH", firstDivision.getState());
        Assert.assertEquals(
                "S.NO.34, HISSA NO. 16 KATRAJ DEHU ROAD BYPASS", firstDivision.getAddressLine1());
        Assert.assertEquals("AMBEGAON (BJK.), KATRAJ", firstDivision.getAddressLine2());

        Assert.assertEquals("SEHGAL WHEELS PRIVATE LIMITED", secondDivision.getOrgName());
        Assert.assertEquals("1-XSXD8T", secondDivision.getDivisionId());
        Assert.assertEquals("1002470-Sales-Akrudi-SegWhl", secondDivision.getDivisionName());
        Assert.assertEquals("1002470", secondDivision.getDealerCode());
        Assert.assertEquals("HAVELI", secondDivision.getTaluka());
        Assert.assertEquals("PUNE", secondDivision.getCity());
        Assert.assertEquals("PUNE", secondDivision.getDistrict());
        Assert.assertEquals("MH", secondDivision.getState());
        Assert.assertEquals("LAKE TOWN B - 10 / 704", secondDivision.getAddressLine1());
        Assert.assertEquals("BIBWEWADI ,", secondDivision.getAddressLine2());

        mockUtils.verifyTimesDivisionListSolrApiWasCalled(1);
    }

    @Test
    public void shouldReturnBadRequestWhenStateOrCityMissingInRequestBody() throws Exception {

        DivisionListRequest request = new DivisionListRequest("", "PUNE");

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/division-search")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andReturn();
        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals("400", errorResponse.getStatus());
        Assert.assertEquals(List.of("state must not be blank"), errorResponse.getErrors());
        mockUtils.verifyTimesDivisionListSolrApiWasCalled(0);
    }

    @Test
    public void shouldReturnEmptyListWhenNoActiveDSMFoundInTheGivenCity() throws Exception {

        URL urlForNoActiveDSMResponse =
                classLoader.getResource(
                        "mockResponses/divisionListResponses/noActiveDSMFoundInCityErrorResponse.json");
        String noActiveDSMResponse = Files.readString(Path.of(urlForNoActiveDSMResponse.toURI()));

        mockUtils.stubDivisionListSOLRApiResponse(noActiveDSMResponse, solarApiRule);
        DivisionListRequest request = new DivisionListRequest("MH", "PUNE");

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/division-search")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        DivisionListResponse response =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), DivisionListResponse.class);

        Assert.assertEquals(0, response.getDivisionList().size());
        mockUtils.verifyTimesDivisionListSolrApiWasCalled(1);
    }

    @Test
    public void shouldReturnEmptyListWhenNoDivisionAvailableForCityStateCombination()
            throws Exception {

        URL urlForNoDivisionAvailableResponse =
                classLoader.getResource(
                        "mockResponses/divisionListResponses/noDivisionAvailableErrorResponse.json");
        String noDivisionAvailableResponse =
                Files.readString(Path.of(urlForNoDivisionAvailableResponse.toURI()));

        mockUtils.stubDivisionListSOLRApiResponse(noDivisionAvailableResponse, solarApiRule);
        DivisionListRequest request = new DivisionListRequest("MH", "PUNE");

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/division-search")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        DivisionListResponse response =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), DivisionListResponse.class);

        Assert.assertEquals(0, response.getDivisionList().size());
        mockUtils.verifyTimesDivisionListSolrApiWasCalled(1);
    }

    @Test
    public void shouldThrowExceptionWhenDivisionListApiReturnsErrorResponse() throws Exception {

        URL divisionListApiResponse =
                classLoader.getResource(
                        "mockResponses/divisionListResponses/divisionListErrorResponse.json");
        String failureResponse = Files.readString(Path.of(divisionListApiResponse.toURI()));

        mockUtils.stubDivisionListSOLRApiResponse(failureResponse, solarApiRule);
        DivisionListRequest request = new DivisionListRequest("MH", "PUNE");

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/division-search")
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

        mockUtils.verifyTimesDivisionListSolrApiWasCalled(1);
    }
}
