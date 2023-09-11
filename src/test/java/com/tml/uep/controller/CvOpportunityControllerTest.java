package com.tml.uep.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.*;
import com.tml.uep.model.entity.CvOpportunity;
import com.tml.uep.repository.CvOpportunityRepository;
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
public class CvOpportunityControllerTest {

    @ClassRule public static WireMockRule solarApiRule = new WireMockRule(8099);

    @Autowired private SolrApiWireMockUtils mockUtils;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper mapper;

    @Autowired private CvOpportunityRepository repository;

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Before
    public void setup() {
        solarApiRule.resetAll();
        repository.deleteAll();
    }

    @Test
    public void shouldCallCvOptyCreationSOLRApiWithEnglishAsSelectedLanguageAndReturn200()
            throws Exception {

        URL fileUrlForCvOptyApiResponse =
                classLoader.getResource(
                        "mockResponses/opportunityResponses/cvOptyCreationSuccessResponse.json");
        String successResponse = Files.readString(Path.of(fileUrlForCvOptyApiResponse.toURI()));

        mockUtils.stubCvOptyCreationSOLRApiResponse(successResponse, solarApiRule);
        CvOptyDetailsRequest request =
                new CvOptyDetailsRequest(
                        "John",
                        "Doe",
                        "919845234679",
                        "Mysore",
                        "KA",
                        "Pickups",
                        "Intra",
                        "Intra V10",
                        "August",
                        "id 1116",
                        "en");

        mockMvc.perform(
                        post("/opportunity-cv")
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CvOpportunity cvOpportunity = repository.findAll().get(0);
        Assert.assertEquals(1, repository.findAll().size());
        Assert.assertEquals(
                request.getDivisionId(), cvOpportunity.getRequest().getDivision().getId());
        Assert.assertEquals(request.getMobileNumber(), cvOpportunity.getPhoneNumber());
        Assert.assertEquals("engagex", cvOpportunity.getRequest().getAppName());
        Assert.assertEquals("Digital-Whatsapp", cvOpportunity.getRequest().getSourceOfContact());
        Assert.assertEquals(
                request.getLikelyPurchaseMonth(),
                cvOpportunity.getRequest().getTmLikelyPurchaseMonth());
        Assert.assertEquals("1", cvOpportunity.getRequest().getQuantity());
        Assert.assertEquals(request.getLob(), cvOpportunity.getRequest().getVcData().getLob());
        Assert.assertEquals(request.getPl(), cvOpportunity.getRequest().getVcData().getPl());
        Assert.assertEquals(request.getPpl(), cvOpportunity.getRequest().getVcData().getPpl());
        Assert.assertEquals("1-DS39WR9", cvOpportunity.getOpportunityId());

        mockUtils.verifyTimesTransliterationApiWasCalled(0);
        mockUtils.verifyTimesCvOpportunityCreationSolrApiWasCalled(1);
    }

    @Test
    public void shouldReturnCvOpportunityBetweenStartAndEndDateAndReturn200() throws Exception {

        List<CvOpportunity> cvOpportunities =
                mapper.readValue(getCvOpportunityList(), new TypeReference<>() {});
        repository.saveAll(cvOpportunities);

        MvcResult mvcResult =
                mockMvc.perform(
                                get("/opportunity-cv")
                                        .param("startDateTime", "2022-12-27T06:22:00Z")
                                        .param("endDateTime", "2022-12-27T06:23:00Z"))
                        .andExpect(status().isOk())
                        .andReturn();
        List<OptyDetailsResponse> responses =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        Assert.assertEquals(3, repository.findAll().size());
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals("1", responses.get(0).getOpportunityId());
        Assert.assertEquals("9010901090", responses.get(0).getPhoneNumber());
        Assert.assertNotNull(responses.get(0).getDateTime());
    }

    @Test
    public void shouldReturnBadRequestWhenStartAndEndDateAreMissingInRequestParam()
            throws Exception {

        MvcResult mvcResult =
                mockMvc.perform(get("/opportunity-cv"))
                        .andExpect(status().isBadRequest())
                        .andReturn();
        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals("400", errorResponse.getStatus());
        Assert.assertEquals(
                List.of("Required String parameter 'startDateTime' is not present"),
                errorResponse.getErrors());
    }

    @Test
    public void shouldReturnEmptyListWhenThereIsNoRecordsForGivenStartAndEndDate()
            throws Exception {

        MvcResult mvcResult =
                mockMvc.perform(
                                get("/opportunity-cv")
                                        .param("startDateTime", "2022-12-23T00:00:00Z")
                                        .param("endDateTime", "2022-12-24T00:00:00Z"))
                        .andExpect(status().isOk())
                        .andReturn();

        Assert.assertEquals(0, repository.findAll().size());
        Assert.assertEquals(List.of().toString(), mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void shouldCallCvOptDetailsSOLRApiAndReturn200() throws Exception {

        URL fileUrlForCvOptyDetailsApiResponse =
                classLoader.getResource(
                        "mockResponses/opportunityResponses/cvOptyDetailsSuccessResponse.json");
        String cvOptyDetailsSuccessResponse =
                Files.readString(Path.of(fileUrlForCvOptyDetailsApiResponse.toURI()));
        mockUtils.stubCvOptyDetailsSOLRApiSuccessResponse(
                cvOptyDetailsSuccessResponse, solarApiRule);
        OpportunityDetailsRequest request =
                mapper.readValue("{\"optyIds\":[\"1-DSOCZYP\"]}", OpportunityDetailsRequest.class);

        MvcResult result =
                mockMvc.perform(
                                post("/opportunity-cv/details")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        SolrOptyDetailsResponse response =
                mapper.readValue(
                        result.getResponse().getContentAsString(), SolrOptyDetailsResponse.class);
        OpportunityDetails opportunityDetails = response.getOptyDetails().get(0);

        Assert.assertEquals("C0 (Prospecting)", opportunityDetails.getOptyStage());
        Assert.assertEquals("9922953090", opportunityDetails.getLeadPhoneNumber());
        Assert.assertEquals(
                "JOSH_BLUE-TATA INTRA V30 AC BS-VI", opportunityDetails.getVehicleDescription());
        Assert.assertEquals("AKURDI", opportunityDetails.getSalesCity());
        Assert.assertEquals(".", opportunityDetails.getPrimaryContactLastName());
        Assert.assertEquals("KV", opportunityDetails.getPrimaryContactFirstName());
        Assert.assertEquals("Intra V30", opportunityDetails.getPl());
        Assert.assertEquals("PUNE", opportunityDetails.getPrimaryContactDistrict());
        Assert.assertEquals("Maharashtra", opportunityDetails.getSalesState());
        Assert.assertEquals("SURESH", opportunityDetails.getLeadFirstName());
        Assert.assertEquals("Digital-WhatsApp", opportunityDetails.getChannel());
        Assert.assertEquals("1002470", opportunityDetails.getDealerCode());
        Assert.assertEquals("First Time", opportunityDetails.getCustomerType());
        Assert.assertEquals("1-DSOCZYP", opportunityDetails.getOptyId());
        Assert.assertEquals("Pickups", opportunityDetails.getLob());
        Assert.assertEquals("N.V.", opportunityDetails.getLeadLastName());
        Assert.assertEquals("MH", opportunityDetails.getOrgState());
        Assert.assertEquals("SEHGAL WHEELS PRIVATE LIMITED", opportunityDetails.getOrgName());

        mockUtils.verifyTimesCvOpportunityDetailsSolrApiWasCalled(1);
    }

    @Test
    public void shouldReturnBadRequestIfOptyIdListIsEmpty() throws Exception {

        OpportunityDetailsRequest request = new OpportunityDetailsRequest(List.of());

        MvcResult result =
                mockMvc.perform(
                                post("/opportunity-cv/details")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andReturn();
        ErrorResponse errorResponse =
                mapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals("400", errorResponse.getStatus());
        Assert.assertEquals(List.of("optyIds must not be empty"), errorResponse.getErrors());
        mockUtils.verifyTimesCvOpportunityDetailsSolrApiWasCalled(0);
    }

    @Test
    public void shouldReturnBadRequestIfOptyIdListIsBlank() throws Exception {

        OpportunityDetailsRequest request = new OpportunityDetailsRequest(List.of(""));

        MvcResult result =
                mockMvc.perform(
                                post("/opportunity-cv/details")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        Assert.assertEquals(
                result.getResponse().getContentAsString(),
                "Opty id list cannot be empty or more than 10");
        mockUtils.verifyTimesCvOpportunityDetailsSolrApiWasCalled(0);
    }

    @Test
    public void shouldReturnBadRequestIfOptyIdListHasMoreThanTenIds() throws Exception {

        OpportunityDetailsRequest request =
                new OpportunityDetailsRequest(
                        List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"));

        MvcResult result =
                mockMvc.perform(
                                post("/opportunity-cv/details")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        Assert.assertEquals(
                result.getResponse().getContentAsString(),
                "Opty id list cannot be empty or more than 10");
        mockUtils.verifyTimesCvOpportunityDetailsSolrApiWasCalled(0);
    }

    @Test
    public void shouldCallTransliterationAPIAndCreateOptyWhenSelectedLanguageIsHindi()
            throws Exception {

        URL transliterationFileUrlResponse =
                classLoader.getResource(
                        "mockResponses/transliterationAPI/successfulTransliterationResponse.json");
        String transliterationResponse =
                Files.readString(Path.of(transliterationFileUrlResponse.toURI()));

        URL fileUrlForCvOptyApiResponse =
                classLoader.getResource(
                        "mockResponses/opportunityResponses/cvOptyCreationSuccessResponse.json");
        String solrApiSuccessResponse =
                Files.readString(Path.of(fileUrlForCvOptyApiResponse.toURI()));

        mockUtils.stubForTransliterationAPISuccessResponse(transliterationResponse, solarApiRule);
        mockUtils.stubCvOptyCreationSOLRApiResponse(solrApiSuccessResponse, solarApiRule);
        CvOptyDetailsRequest request =
                new CvOptyDetailsRequest(
                        "John",
                        "Doe",
                        "919845234679",
                        "Mysore",
                        "KA",
                        "Pickups",
                        "Intra",
                        "Intra V10",
                        "August",
                        "id 1116",
                        "hi");

        mockMvc.perform(
                        post("/opportunity-cv")
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CvOpportunity cvOpportunity = repository.findAll().get(0);
        Assert.assertEquals(1, repository.findAll().size());
        Assert.assertEquals(
                request.getDivisionId(), cvOpportunity.getRequest().getDivision().getId());
        Assert.assertEquals(request.getMobileNumber(), cvOpportunity.getPhoneNumber());
        Assert.assertEquals("engagex", cvOpportunity.getRequest().getAppName());
        Assert.assertEquals("Digital-Whatsapp", cvOpportunity.getRequest().getSourceOfContact());
        Assert.assertEquals(
                request.getLikelyPurchaseMonth(),
                cvOpportunity.getRequest().getTmLikelyPurchaseMonth());
        Assert.assertEquals("1", cvOpportunity.getRequest().getQuantity());
        Assert.assertEquals(request.getLob(), cvOpportunity.getRequest().getVcData().getLob());
        Assert.assertEquals(request.getPl(), cvOpportunity.getRequest().getVcData().getPl());
        Assert.assertEquals(request.getPpl(), cvOpportunity.getRequest().getVcData().getPpl());
        Assert.assertEquals("1-DS39WR9", cvOpportunity.getOpportunityId());

        mockUtils.verifyTimesTransliterationApiWasCalled(1);
        mockUtils.verifyTimesCvOpportunityCreationSolrApiWasCalled(1);
    }

    @Test
    public void shouldSaveOptyCreationDetailsEvenIfOptyAPIReturnsFailureResponseWithNoOptyId()
            throws Exception {

        URL fileUrlForCvOptyApiResponse =
                classLoader.getResource(
                        "mockResponses/opportunityResponses/cvOptyCreationFailureResponse.json");
        String failureResponse = Files.readString(Path.of(fileUrlForCvOptyApiResponse.toURI()));

        mockUtils.stubCvOptyCreationSOLRApiResponse(failureResponse, solarApiRule);
        CvOptyDetailsRequest request =
                new CvOptyDetailsRequest(
                        "John",
                        "Doe",
                        "919845234679",
                        "Mysore",
                        "KA",
                        "Pickups",
                        "Intra",
                        "Intra V10",
                        "August",
                        "id 1116",
                        "en");

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/opportunity-cv")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is5xxServerError())
                        .andReturn();

        Assert.assertEquals(0, repository.findAll().size());
        Assert.assertEquals(
                "{\n" + "  \"msg\": \"Invalid last name.\",\n" + "  \"error_code\": 295\n" + "}",
                mvcResult.getResolvedException().getMessage());

        mockUtils.verifyTimesTransliterationApiWasCalled(0);
        mockUtils.verifyTimesCvOpportunityCreationSolrApiWasCalled(1);
    }

    private String getCvOpportunityList() {
        return "[\n"
                + "  {\n"
                + "    \"id\": 1,\n"
                + "    \"phoneNumber\": \"9010901090\",\n"
                + "    \"request\": {\n"
                + "      \"contact\": {\n"
                + "        \"city\": \"Pune\",\n"
                + "        \"state\": \"MH\",\n"
                + "        \"first_name\": \"Vishal\",\n"
                + "        \"last_name\": \"A\",\n"
                + "        \"mobile_number\": \"9010901090\"\n"
                + "      },\n"
                + "      \"quantity\": \"1\",\n"
                + "      \"division\": {\n"
                + "        \"id\": \"11\"\n"
                + "      },\n"
                + "      \"source_of_contact\": \"1\",\n"
                + "      \"lob_information\": {\n"
                + "        \"vehicle_application\": \"vehicle app\",\n"
                + "        \"customer_type\": \"customer\",\n"
                + "        \"body_type\": \"body\",\n"
                + "        \"usage_category\": \"PV\"\n"
                + "      },\n"
                + "      \"tm_likely_purchase_month\": \"Dec\",\n"
                + "      \"vc_data\": {\n"
                + "        \"lob\": \"lob\",\n"
                + "        \"ppl\": \"ppl\",\n"
                + "        \"pl\": \"pl\"\n"
                + "      },\n"
                + "      \"appname\": \"enagageX\"\n"
                + "    },\n"
                + "    \"opportunityId\": \"1\",\n"
                + "    \"dateTime\": \"2022-12-27T11:52:35.293079+05:30\"\n"
                + "  },\n"
                + "  {\n"
                + "    \"id\": 2,\n"
                + "    \"phoneNumber\": \"9890781891\",\n"
                + "    \"request\": {\n"
                + "      \"contact\": {\n"
                + "        \"city\": \"Pune\",\n"
                + "        \"state\": \"MH\",\n"
                + "        \"first_name\": \"Mayur\",\n"
                + "        \"last_name\": \"T\",\n"
                + "        \"mobile_number\": \"9890781891\"\n"
                + "      },\n"
                + "      \"quantity\": \"1\",\n"
                + "      \"division\": {\n"
                + "        \"id\": \"22\"\n"
                + "      },\n"
                + "      \"source_of_contact\": \"1\",\n"
                + "      \"lob_information\": {\n"
                + "        \"vehicle_application\": \"vehicle app\",\n"
                + "        \"customer_type\": \"customer\",\n"
                + "        \"body_type\": \"body\",\n"
                + "        \"usage_category\": \"PV\"\n"
                + "      },\n"
                + "      \"tm_likely_purchase_month\": \"Dec\",\n"
                + "      \"vc_data\": {\n"
                + "        \"lob\": \"lob\",\n"
                + "        \"ppl\": \"ppl\",\n"
                + "        \"pl\": \"pl\"\n"
                + "      },\n"
                + "      \"appname\": \"enagageX\"\n"
                + "    },\n"
                + "    \"opportunityId\": \"\",\n"
                + "    \"dateTime\": \"2022-12-27T11:52:35.293144+05:30\"\n"
                + "  },\n"
                + "  {\n"
                + "    \"id\": 3,\n"
                + "    \"phoneNumber\": \"8347894589\",\n"
                + "    \"request\": {\n"
                + "      \"contact\": {\n"
                + "        \"city\": \"Bangalore\",\n"
                + "        \"state\": \"KA\",\n"
                + "        \"first_name\": \"Tarun\",\n"
                + "        \"last_name\": \"A\",\n"
                + "        \"mobile_number\": \"8347894589\"\n"
                + "      },\n"
                + "      \"quantity\": \"1\",\n"
                + "      \"division\": {\n"
                + "        \"id\": \"15\"\n"
                + "      },\n"
                + "      \"source_of_contact\": \"1\",\n"
                + "      \"lob_information\": {\n"
                + "        \"vehicle_application\": \"vehicle app\",\n"
                + "        \"customer_type\": \"customer\",\n"
                + "        \"body_type\": \"body\",\n"
                + "        \"usage_category\": \"CV\"\n"
                + "      },\n"
                + "      \"tm_likely_purchase_month\": \"Dec\",\n"
                + "      \"vc_data\": {\n"
                + "        \"lob\": \"lob\",\n"
                + "        \"ppl\": \"ppl\",\n"
                + "        \"pl\": \"pl\"\n"
                + "      },\n"
                + "      \"appname\": \"enagageX\"\n"
                + "    },\n"
                + "    \"opportunityId\": \"6\",\n"
                + "    \"dateTime\": \"2022-11-20T12:56:58.298364+05:30\"\n"
                + "  }\n"
                + "]";
    }
}
