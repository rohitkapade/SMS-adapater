package com.tml.uep.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.OpportunityCreationRequest;
import com.tml.uep.model.entity.Opportunity;
import com.tml.uep.model.entity.ProductLineVcMapping;
import com.tml.uep.repository.OpportunityAuditRepository;
import com.tml.uep.repository.ProductLineVcMappingRepository;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(AWSTestConfiguration.class)
public class OpportunityControllerTest {

    @ClassRule public static WireMockRule solarApiRule = new WireMockRule(8099);

    @Autowired private SolrApiWireMockUtils mockUtils;

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper mapper;

    @Autowired private OpportunityAuditRepository opportunityAuditRepository;

    @Autowired private ProductLineVcMappingRepository productLineVcMappingRepository;

    private ProductLineVcMapping productLineVcMapping;

    @Before
    public void setUp() {
        solarApiRule.resetRequests();
        opportunityAuditRepository.deleteAll();
        productLineVcMappingRepository.deleteAll();
        productLineVcMapping =
                new ProductLineVcMapping("some_lob", "Harrier", "Harrier 123abc", "vc_number123");
        productLineVcMapping = productLineVcMappingRepository.save(productLineVcMapping);
    }

    @Test
    public void shouldCallOptyCreationSOLRApiAndSaveOptyDetailsInDBAndReturn200() throws Exception {

        URL fileUrlForCustomerApiResponse =
                classLoader.getResource("mockResponses/customerDetails/validResponse.json");
        String customerApiResponse =
                Files.readString(Path.of(fileUrlForCustomerApiResponse.toURI()));
        mockUtils.stubCustomerSolarApiResponse(customerApiResponse, solarApiRule);

        URL fileUrlForOptyCreationApiResponse =
                classLoader.getResource(
                        "mockResponses/opportunityResponses/optyCreationSuccessResponse.json");
        String optyCreationApiResponse =
                Files.readString(Path.of(fileUrlForOptyCreationApiResponse.toURI()));
        mockUtils.stubOptyCreationSOLRApiSuccessResponse(optyCreationApiResponse, solarApiRule);

        OpportunityCreationRequest requestPayload =
                new OpportunityCreationRequest(
                        "conv123",
                        "FAROOQ",
                        "S.",
                        "9908523366",
                        "KURNOOL",
                        "560034",
                        "Andhra Pradesh",
                        "division_id_123",
                        productLineVcMapping.getId(),
                        null,
                        LocalDate.of(2021, 10, 29));

        mockMvc.perform(
                        post("/opportunity/create")
                                .content(mapper.writeValueAsString(requestPayload))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        mockUtils.verifyTimesCustomerSolrApiWasCalled(1);
        mockUtils.verifyTimesOpportunityCreationSolrApiWasCalled(1);

        List<Opportunity> opportunityList = opportunityAuditRepository.findAll();

        assertEquals(1, opportunityList.size());
        assertEquals("5-CDWKGL7", opportunityList.get(0).getOptyId());
        assertEquals("conv123", opportunityList.get(0).getConversationId());
        assertEquals("9908523366", opportunityList.get(0).getPhoneNumber());
        assertNotNull(opportunityList.get(0).getOptyCreationDateTime());
    }

    @Test
    public void shouldNOTCallOptyCreationSOLRApiWhenNoVCDetailsFoundInDBAndReturn204()
            throws Exception {
        productLineVcMappingRepository.deleteAll();
        OpportunityCreationRequest requestPayload =
                new OpportunityCreationRequest(
                        "conv123",
                        "FAROOQ",
                        "S.",
                        "9908523366",
                        "KURNOOL",
                        "560034",
                        "Andhra Pradesh",
                        "division_id_123",
                        1L,
                        null,
                        LocalDate.of(2021, 10, 29));

        mockMvc.perform(
                        post("/opportunity/create")
                                .content(mapper.writeValueAsString(requestPayload))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        mockUtils.verifyTimesCustomerSolrApiWasCalled(0);
        mockUtils.verifyTimesOpportunityCreationSolrApiWasCalled(0);

        List<Opportunity> opportunityList = opportunityAuditRepository.findAll();

        assertEquals(0, opportunityList.size());
    }

    @Test
    public void
            shouldCallOptyCreationSOLRApiWhenNoCustomerInfoFoundFromCustomerSolrAPIAndReturn200()
                    throws Exception {
        ProductLineVcMapping productLineVcMapping =
                new ProductLineVcMapping("some_lob", "Harrier", "Harrier 123abc", "vc_number123");
        productLineVcMappingRepository.save(productLineVcMapping);

        mockUtils.stubCustomerSolarApiResponse(
                String.valueOf(Collections.EMPTY_LIST), solarApiRule);

        URL fileUrlForOptyCreationApiResponse =
                classLoader.getResource(
                        "mockResponses/opportunityResponses/optyCreationSuccessResponse.json");
        String optyCreationApiResponse =
                Files.readString(Path.of(fileUrlForOptyCreationApiResponse.toURI()));
        mockUtils.stubOptyCreationSOLRApiSuccessResponse(optyCreationApiResponse, solarApiRule);

        OpportunityCreationRequest requestPayload =
                new OpportunityCreationRequest(
                        "conv123",
                        "John",
                        "",
                        "787867676",
                        "Bangalore",
                        "560034",
                        "Karnataka",
                        "division_id_ABC",
                        productLineVcMapping.getId(),
                        "1",
                        LocalDate.of(2021, 10, 29));

        mockMvc.perform(
                        post("/opportunity/create")
                                .content(mapper.writeValueAsString(requestPayload))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        mockUtils.verifyTimesCustomerSolrApiWasCalled(1);
        mockUtils.verifyTimesOpportunityCreationSolrApiWasCalled(1);

        List<Opportunity> opportunityList = opportunityAuditRepository.findAll();

        assertEquals(1, opportunityList.size());
        assertEquals("5-CDWKGL7", opportunityList.get(0).getOptyId());
        assertEquals("conv123", opportunityList.get(0).getConversationId());
        assertEquals("787867676", opportunityList.get(0).getPhoneNumber());
        assertNotNull(opportunityList.get(0).getOptyCreationDateTime());
    }

    @Test
    public void
            shouldReturn500InternalServerErrorWhenSomeExceptionHasOccurredWhileCallingSolrAPIForOptyCreation()
                    throws Exception {
        URL fileUrlForCustomerApiResponse =
                classLoader.getResource("mockResponses/customerDetails/validResponse.json");
        String customerApiResponse =
                Files.readString(Path.of(fileUrlForCustomerApiResponse.toURI()));
        mockUtils.stubCustomerSolarApiResponse(customerApiResponse, solarApiRule);

        mockUtils.stubOptyCreationSOLRApi500ServerErrorResponse(solarApiRule);

        OpportunityCreationRequest requestPayload =
                new OpportunityCreationRequest(
                        "conv123",
                        "FAROOQ",
                        "S.",
                        "9908523366",
                        "KURNOOL",
                        "560034",
                        "Andhra Pradesh",
                        "division_id_123",
                        productLineVcMapping.getId(),
                        null,
                        LocalDate.of(2021, 10, 29));

        mockMvc.perform(
                        post("/opportunity/create")
                                .content(mapper.writeValueAsString(requestPayload))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andReturn();

        mockUtils.verifyTimesCustomerSolrApiWasCalled(1);
        mockUtils.verifyTimesOpportunityCreationSolrApiWasCalled(1);

        List<Opportunity> opportunityList = opportunityAuditRepository.findAll();

        assertEquals(0, opportunityList.size());
    }
}
