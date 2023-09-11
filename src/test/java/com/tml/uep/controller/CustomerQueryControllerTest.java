package com.tml.uep.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.ErrorResponse;
import com.tml.uep.model.dto.customerquery.CustomerQueryDTO;
import com.tml.uep.model.dto.customerquery.CustomerQueryStatus;
import com.tml.uep.model.dto.customerquery.CustomerQueryUpdateRequest;
import com.tml.uep.model.entity.CustomerQuery;
import com.tml.uep.repository.CustomerQueryRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.tml.uep.constants.Constants;
import org.junit.Assert;
import org.junit.Before;
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
public class CustomerQueryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CustomerQueryRepository customerQueryRepository;
    @Autowired private ObjectMapper mapper;

    @Before
    public void setup() {
        customerQueryRepository.deleteAll();
    }

    //#region Update Customer Query Tests
    @Test
    public void shouldCaptureCorrectStatusAndAssignedToFieldsWhenCustomerQueryIsUpdated() throws Exception {

        //Given
        CustomerQuery existingCustomerQuery = new CustomerQuery("customer1", "9004244123", "Some query", 123L, CustomerQueryStatus.NOT_STARTED, null);
        existingCustomerQuery = customerQueryRepository.save(existingCustomerQuery);

        //When

        CustomerQueryUpdateRequest updateRequest = new CustomerQueryUpdateRequest(CustomerQueryStatus.WORK_IN_PROGRESS, "agent1", "agent1");
        mockMvc.perform(
                put("/customer-query/"+existingCustomerQuery.getId().toString())
                        .content(mapper.writeValueAsString(updateRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        //Then
        CustomerQuery updatedCustomerQuery = customerQueryRepository.findById(existingCustomerQuery.getId()).get();
        Assert.assertEquals(updatedCustomerQuery.getStatus(), updateRequest.getStatus());
        Assert.assertEquals(updatedCustomerQuery.getAssignedTo(), updateRequest.getAssignedTo());
        Assert.assertEquals(updatedCustomerQuery.getUpdatedBy(), updateRequest.getUpdatedBy());
        Assert.assertEquals(updatedCustomerQuery.getCustomerId(), existingCustomerQuery.getCustomerId());
        Assert.assertEquals(updatedCustomerQuery.getQuery(), existingCustomerQuery.getQuery());
        Assert.assertEquals(updatedCustomerQuery.getMobileNumber(), existingCustomerQuery.getMobileNumber());
        Assert.assertEquals(updatedCustomerQuery.getImageId(), existingCustomerQuery.getImageId());
        Assert.assertNotNull(updatedCustomerQuery.getUpdatedAt());
    }

    @Test
    public void shouldReturnBadRequestErrorIfQueryIdIsNotFound() throws Exception {
        //When
        CustomerQueryUpdateRequest updateRequest = new CustomerQueryUpdateRequest(CustomerQueryStatus.WORK_IN_PROGRESS, "agent1", "agent1");
        MvcResult mvcResult = mockMvc.perform(
                        put("/customer-query/1")
                                .content(mapper.writeValueAsString(updateRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andReturn();
        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        //Then
        Assert.assertEquals("400", errorResponse.getStatus());
        Assert.assertEquals(List.of(Constants.INVALID_QUERY_ID_MESSAGE), errorResponse.getErrors());
    }

    @Test
    public void shouldReturnBadRequestErrorIfStatusIsMissingInTheRequest() throws Exception {
        //When
        MvcResult mvcResult = mockMvc.perform(
                        put("/customer-query/1")
                                .content("{\"assignedTo\":\"agent1\", \"updatedBy\": \"agent1\"}")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andReturn();
        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        //Then
        Assert.assertEquals("400", errorResponse.getStatus());
        Assert.assertEquals(List.of("status must not be null"), errorResponse.getErrors());
    }

    @Test
    public void shouldReturnBadRequestErrorIfAssignedToIsMissingInTheRequest() throws Exception {
        //When
        MvcResult mvcResult = mockMvc.perform(
                        put("/customer-query/1")
                                .content("{\"status\":\"WORK_IN_PROGRESS\", \"updatedBy\": \"agent1\"}")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andReturn();
        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        //Then
        Assert.assertEquals("400", errorResponse.getStatus());
        Assert.assertEquals(List.of("assignedTo must not be blank"), errorResponse.getErrors());
    }

    @Test
    public void shouldReturnBadRequestErrorIfUpdatedByIsMissingInTheRequest() throws Exception {
        //When
        MvcResult mvcResult = mockMvc.perform(
                        put("/customer-query/1")
                                .content("{\"status\":\"WORK_IN_PROGRESS\",\"assignedTo\":\"agent1\"}")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andReturn();
        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        //Then
        Assert.assertEquals("400", errorResponse.getStatus());
        Assert.assertEquals(List.of("updatedBy must not be blank"), errorResponse.getErrors());
    }

    //#endregion Update Customer Query Tests

    //region Customer Query List tests
    @Test
    public void shouldReturnAllCustomerQueriesBetweenGivenDateTimeRange() throws Exception {
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        //Given
        List<CustomerQuery> existingQueries = List.of(new CustomerQuery("customer1", "9004244123", "Some query", 123L, CustomerQueryStatus.NOT_STARTED, null),
                new CustomerQuery("customer2", "9004244124", "Some query 2", null, CustomerQueryStatus.NOT_STARTED, null));
        customerQueryRepository.saveAll(existingQueries);
        OffsetDateTime end = start.plusSeconds(2L);

        //When
        MvcResult mvcResult = mockMvc.perform(get("/customer-query")
                .param("fromDateTime", start.toString())
                .param("toDateTime", end.toString())
        ).andExpect(status().isOk()).andReturn();

        List<CustomerQueryDTO> queries =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(queries);
        Assert.assertEquals(queries.size(), 2);
        CustomerQuery firstExistingQuery = existingQueries.get(0);
        CustomerQueryDTO firstReturnedQuery = queries.get(0);
        Assert.assertEquals(firstExistingQuery.getCustomerId(), firstReturnedQuery.getCustomerId());
        Assert.assertEquals(firstExistingQuery.getQuery(), firstReturnedQuery.getQuery());
        Assert.assertEquals(firstExistingQuery.getMobileNumber(), firstReturnedQuery.getMobileNumber());
        Assert.assertNotNull(firstReturnedQuery.getCreatedAt());

        CustomerQuery secondExistingQuery = existingQueries.get(1);
        CustomerQueryDTO secondReturnedQuery = queries.get(1);
        Assert.assertEquals(secondExistingQuery.getCustomerId(), secondReturnedQuery.getCustomerId());
        Assert.assertEquals(secondExistingQuery.getQuery(), secondReturnedQuery.getQuery());
        Assert.assertEquals(secondExistingQuery.getMobileNumber(), secondReturnedQuery.getMobileNumber());
        Assert.assertNotNull(secondReturnedQuery.getCreatedAt());
    }

    @Test
    public void shouldNotReturnAnyCustomerQueriesOutsideOfGivenDateTimeRange() throws Exception {
        //Given
        List<CustomerQuery> existingQueries = List.of(new CustomerQuery("customer1", "9004244123", "Some query", 123L, CustomerQueryStatus.NOT_STARTED, null),
                new CustomerQuery("customer2", "9004244124", "Some query 2", null, CustomerQueryStatus.NOT_STARTED, null));
        customerQueryRepository.saveAll(existingQueries);
        OffsetDateTime start = OffsetDateTime.parse("2023-09-08T08:29:00.000-00:00");
        OffsetDateTime end = OffsetDateTime.parse("2023-09-09T08:29:00.000-00:00");

        //When
        MvcResult mvcResult = mockMvc.perform(get("/customer-query")
                .param("fromDateTime", start.toString())
                .param("toDateTime", end.toString())
        ).andExpect(status().isOk()).andReturn();

        List<CustomerQueryDTO> queries =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(queries);
        Assert.assertEquals(queries.size(), 0);
    }

    @Test
    public void shouldReturnAllCustomerQueriesBetweenGivenDateTimeRangeForGivenStatus() throws Exception {
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        //Given
        List<CustomerQuery> existingQueries = List.of(new CustomerQuery("customer1", "9004244123", "Some query", 123L, CustomerQueryStatus.NOT_STARTED, null),
                new CustomerQuery("customer2", "9004244124", "Some query 2", null, CustomerQueryStatus.WORK_IN_PROGRESS, null));
        customerQueryRepository.saveAll(existingQueries);
        OffsetDateTime end = start.plusSeconds(2L);

        //When
        MvcResult mvcResult = mockMvc.perform(get("/customer-query")
                .param("fromDateTime", start.toString())
                .param("toDateTime", end.toString())
                .param("status", CustomerQueryStatus.NOT_STARTED.name())
        ).andExpect(status().isOk()).andReturn();

        List<CustomerQueryDTO> queries =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(queries);
        Assert.assertEquals(queries.size(), 1);
        CustomerQuery firstExistingQuery = existingQueries.get(0);
        CustomerQueryDTO returnedQuery = queries.get(0);
        Assert.assertEquals(CustomerQueryStatus.NOT_STARTED, firstExistingQuery.getStatus());
        Assert.assertEquals(firstExistingQuery.getCustomerId(), returnedQuery.getCustomerId());
        Assert.assertEquals(firstExistingQuery.getQuery(), returnedQuery.getQuery());
        Assert.assertEquals(firstExistingQuery.getMobileNumber(), returnedQuery.getMobileNumber());
        Assert.assertNotNull(returnedQuery.getCreatedAt());
    }

    @Test
    public void shouldReturnAllCustomerQueriesBetweenGivenDateTimeRangeAssignedToGivenAgent() throws Exception {
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        //Given
        List<CustomerQuery> existingQueries = List.of(new CustomerQuery("customer1", "9004244123", "Some query", 123L, CustomerQueryStatus.NOT_STARTED, "agent1"),
                new CustomerQuery("customer2", "9004244124", "Some query 2", null, CustomerQueryStatus.WORK_IN_PROGRESS, null),
                new CustomerQuery("customer3", "9004244125", "Some query 3", null, CustomerQueryStatus.WORK_IN_PROGRESS, "agent1"),
                new CustomerQuery("customer4", "9004244126", "Some query 4", null, CustomerQueryStatus.WORK_IN_PROGRESS, "agent2"));
        customerQueryRepository.saveAll(existingQueries);
        OffsetDateTime end = start.plusSeconds(2L);

        //When
        MvcResult mvcResult = mockMvc.perform(get("/customer-query")
                .param("fromDateTime", start.toString())
                .param("toDateTime", end.toString())
                .param("assignedTo", "agent1")
        ).andExpect(status().isOk()).andReturn();

        List<CustomerQueryDTO> queries =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(queries);
        Assert.assertEquals(queries.size(), 2);
        CustomerQuery firstQuery = existingQueries.get(0);
        CustomerQueryDTO firstReturnedQuery = queries.get(0);
        Assert.assertEquals(firstQuery.getCustomerId(), firstReturnedQuery.getCustomerId());
        Assert.assertEquals(firstQuery.getQuery(), firstReturnedQuery.getQuery());
        Assert.assertEquals(firstQuery.getMobileNumber(), firstReturnedQuery.getMobileNumber());
        Assert.assertNotNull(firstReturnedQuery.getCreatedAt());

        CustomerQuery secondQuery = existingQueries.get(2);
        CustomerQueryDTO secondReturnedQuery = queries.get(1);
        Assert.assertEquals(secondQuery.getAssignedTo(), secondReturnedQuery.getAssignedTo());
        Assert.assertEquals(secondQuery.getCustomerId(), secondReturnedQuery.getCustomerId());
        Assert.assertEquals(secondQuery.getQuery(), secondReturnedQuery.getQuery());
        Assert.assertEquals(secondQuery.getMobileNumber(), secondReturnedQuery.getMobileNumber());
        Assert.assertNotNull(secondReturnedQuery.getCreatedAt());
    }

    @Test
    public void shouldReturnAllCustomerQueriesBetweenGivenDateTimeRangeAssignedToGivenAgentWithGivenStatus() throws Exception {
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        //Given
        List<CustomerQuery> existingQueries = List.of(new CustomerQuery("customer1", "9004244123", "Some query", 123L, CustomerQueryStatus.NOT_STARTED, "agent1"),
                new CustomerQuery("customer2", "9004244124", "Some query 2", null, CustomerQueryStatus.WORK_IN_PROGRESS, null),
                new CustomerQuery("customer3", "9004244125", "Some query 3", null, CustomerQueryStatus.WORK_IN_PROGRESS, "agent1"),
                new CustomerQuery("customer4", "9004244126", "Some query 4", null, CustomerQueryStatus.WORK_IN_PROGRESS, "agent2"),
                new CustomerQuery("customer5", "9004244127", "Some query 5", 121L, CustomerQueryStatus.NOT_STARTED, "agent2"),
                new CustomerQuery("customer6", "9004244128", "Some query 6", 122L, CustomerQueryStatus.CLOSED, "agent2"));
        customerQueryRepository.saveAll(existingQueries);
        OffsetDateTime end = start.plusSeconds(2L);

        //When
        MvcResult mvcResult = mockMvc.perform(get("/customer-query")
                .param("fromDateTime", start.toString())
                .param("toDateTime", end.toString())
                .param("assignedTo", "agent2")
                .param("status", CustomerQueryStatus.CLOSED.name())
        ).andExpect(status().isOk()).andReturn();

        List<CustomerQueryDTO> queries =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(queries);
        Assert.assertEquals(queries.size(), 1);
        CustomerQuery firstQuery = existingQueries.get(5);
        CustomerQueryDTO returnedQuery = queries.get(0);
        Assert.assertEquals(firstQuery.getAssignedTo(), returnedQuery.getAssignedTo());
        Assert.assertEquals(firstQuery.getStatus(), returnedQuery.getStatus());
        Assert.assertEquals(firstQuery.getCustomerId(), returnedQuery.getCustomerId());
        Assert.assertEquals(firstQuery.getQuery(), returnedQuery.getQuery());
        Assert.assertEquals(firstQuery.getMobileNumber(), returnedQuery.getMobileNumber());
        Assert.assertNotNull(returnedQuery.getCreatedAt());
    }

    @Test
    public void shouldReturnAllCustomerQueriesWhenNoQueryParamIsPassed() throws Exception {
        //Given
        List<CustomerQuery> existingQueries = List.of(new CustomerQuery("customer1", "9004244123", "Some query", 123L, CustomerQueryStatus.NOT_STARTED, "agent1"),
                new CustomerQuery("customer2", "9004244124", "Some query 2", null, CustomerQueryStatus.WORK_IN_PROGRESS, null),
                new CustomerQuery("customer3", "9004244125", "Some query 3", null, CustomerQueryStatus.WORK_IN_PROGRESS, "agent1"),
                new CustomerQuery("customer4", "9004244126", "Some query 4", null, CustomerQueryStatus.WORK_IN_PROGRESS, "agent2"),
                new CustomerQuery("customer5", "9004244127", "Some query 5", 121L, CustomerQueryStatus.NOT_STARTED, "agent2"),
                new CustomerQuery("customer6", "9004244128", "Some query 6", 122L, CustomerQueryStatus.CLOSED, "agent2"));
        customerQueryRepository.saveAll(existingQueries);

        //When
        MvcResult mvcResult = mockMvc.perform(get("/customer-query")
        ).andExpect(status().isOk()).andReturn();

        List<CustomerQueryDTO> queries =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(queries);
        Assert.assertEquals(queries.size(), 6);
    }

    @Test
    public void shouldReturnBadRequestIfInvalidStatusIsPassed() throws Exception {

        //When
        MvcResult mvcResult = mockMvc.perform(get("/customer-query")
                .param("status", "CLOSE")
        ).andExpect(status().isBadRequest()).andReturn();


        System.out.println(mvcResult.getResponse().getContentAsString());

        ErrorResponse errorResponse =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        Assert.assertNotNull(errorResponse);
        Assert.assertEquals("Invalid value for parameter status", errorResponse.getErrors().get(0));
    }

    //endregion Customer Query List tests

}
