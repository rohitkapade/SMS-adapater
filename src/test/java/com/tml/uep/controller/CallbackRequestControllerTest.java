package com.tml.uep.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.CallbackRequestStatus;
import com.tml.uep.model.ErrorResponse;
import com.tml.uep.model.dto.callbackrequest.CallbackRequest;
import com.tml.uep.model.dto.callbackrequest.CallbackUpdateRequest;
import com.tml.uep.model.entity.CallbackRequestEntity;
import com.tml.uep.repository.CallbackRequestRepository;
import com.tml.uep.service.CallbackRequestService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.*;
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
public class CallbackRequestControllerTest {

    @Autowired private CallbackRequestService service;
    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper mapper;

    @Autowired private CallbackRequestRepository repository;

    @Before
    public void setupAll() {
        repository.deleteAll();
    }

    @Test
    public void shouldReturnAllCallbackRequestsBetweenGivenDateTimeRange() throws Exception {
        OffsetDateTime endDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startDateTime = endDateTime.minusMinutes(180);
        //Given
        List<CallbackRequestEntity> existingCallbackRequests = List.of(
                new CallbackRequestEntity(startDateTime, endDateTime, "9004244124", "cust1", null, CallbackRequestStatus.NOT_STARTED),
                new CallbackRequestEntity(startDateTime, endDateTime.minusMinutes(10), "9004244125", "cust2", null, CallbackRequestStatus.NOT_STARTED));
        repository.saveAll(existingCallbackRequests);


        //When
        MvcResult mvcResult = mockMvc.perform(get("/callback-request")
                .param("startDateTime", startDateTime.toString())
                .param("endDateTime", endDateTime.toString())
        ).andExpect(status().isOk()).andReturn();

        List<CallbackRequest> callbackRequests =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(callbackRequests);
        Assert.assertEquals(callbackRequests.size(), 2);
        CallbackRequestEntity firstExistingCallback = existingCallbackRequests.get(0);
        CallbackRequest firstReturnedCallback = callbackRequests.get(0);
        Assert.assertEquals(firstExistingCallback.getCustomerId(), firstReturnedCallback.getCustomerId());
        Assert.assertEquals(firstExistingCallback.getAssignedTo(), firstReturnedCallback.getAssignedTo());
        Assert.assertEquals(firstExistingCallback.getMobileNumber(), firstReturnedCallback.getMobileNumber());
        Assert.assertEquals(firstExistingCallback.getUpdatedBy(), firstReturnedCallback.getUpdatedBy());
        Assert.assertEquals(firstExistingCallback.getStartDateTime().toEpochSecond(), firstReturnedCallback.getStartDateTime().toEpochSecond());
        Assert.assertEquals(firstExistingCallback.getEndDateTime().toEpochSecond(), firstReturnedCallback.getEndDateTime().toEpochSecond());
        Assert.assertNotNull(firstReturnedCallback.getCreatedAt());

        CallbackRequestEntity secondExistingCallback = existingCallbackRequests.get(1);
        CallbackRequest secondReturnedCallback = callbackRequests.get(1);
        Assert.assertEquals(secondExistingCallback.getCustomerId(), secondReturnedCallback.getCustomerId());
        Assert.assertEquals(secondExistingCallback.getAssignedTo(), secondReturnedCallback.getAssignedTo());
        Assert.assertEquals(secondExistingCallback.getMobileNumber(), secondReturnedCallback.getMobileNumber());
        Assert.assertEquals(secondExistingCallback.getUpdatedBy(), secondReturnedCallback.getUpdatedBy());
        Assert.assertEquals(secondExistingCallback.getStartDateTime().toEpochSecond(), secondReturnedCallback.getStartDateTime().toEpochSecond());
        Assert.assertEquals(secondExistingCallback.getEndDateTime().toEpochSecond(), secondReturnedCallback.getEndDateTime().toEpochSecond());
        Assert.assertNotNull(secondReturnedCallback.getCreatedAt());
    }

    @Test
    public void shouldReturnAllCallbackRequestsAfterStartDateTimePassedInTheRequest() throws Exception {
        OffsetDateTime endDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startDateTime = endDateTime.minusMinutes(180);
        //Given
        List<CallbackRequestEntity> existingCallbackRequests = List.of(
                new CallbackRequestEntity(startDateTime, endDateTime, "9004244124", "cust1", null, CallbackRequestStatus.NOT_STARTED),
                new CallbackRequestEntity(startDateTime.plusMinutes(10), endDateTime.minusMinutes(10), "9004244125", "cust2", null, CallbackRequestStatus.NOT_STARTED));
        repository.saveAll(existingCallbackRequests);


        //When
        MvcResult mvcResult = mockMvc.perform(get("/callback-request")
                .param("startDateTime", startDateTime.plusMinutes(10).toString())
        ).andExpect(status().isOk()).andReturn();

        List<CallbackRequest> callbackRequests =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(callbackRequests);
        Assert.assertEquals(callbackRequests.size(), 1);
        CallbackRequestEntity secondExistingCallback = existingCallbackRequests.get(1);
        CallbackRequest returnedCallback = callbackRequests.get(0);
        Assert.assertEquals(secondExistingCallback.getCustomerId(), returnedCallback.getCustomerId());
        Assert.assertEquals(secondExistingCallback.getAssignedTo(), returnedCallback.getAssignedTo());
        Assert.assertEquals(secondExistingCallback.getMobileNumber(), returnedCallback.getMobileNumber());
        Assert.assertEquals(secondExistingCallback.getUpdatedBy(), returnedCallback.getUpdatedBy());
        Assert.assertEquals(secondExistingCallback.getStartDateTime().toEpochSecond(), returnedCallback.getStartDateTime().toEpochSecond());
        Assert.assertEquals(secondExistingCallback.getEndDateTime().toEpochSecond(), returnedCallback.getEndDateTime().toEpochSecond());
        Assert.assertNotNull(returnedCallback.getCreatedAt());
    }

    @Test
    public void shouldReturnAllCallbackRequestsBeforeEndDateTimePassedInTheRequest() throws Exception {
        OffsetDateTime endDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startDateTime = endDateTime.minusMinutes(180);
        //Given
        List<CallbackRequestEntity> existingCallbackRequests = List.of(
                new CallbackRequestEntity(startDateTime, endDateTime, "9004244124", "cust1", null, CallbackRequestStatus.NOT_STARTED),
                new CallbackRequestEntity(startDateTime.plusMinutes(10), endDateTime.minusMinutes(10), "9004244125", "cust2", null, CallbackRequestStatus.NOT_STARTED));
        repository.saveAll(existingCallbackRequests);


        //When
        MvcResult mvcResult = mockMvc.perform(get("/callback-request")
                .param("endDateTime", endDateTime.minusMinutes(10).toString())
        ).andExpect(status().isOk()).andReturn();

        List<CallbackRequest> callbackRequests =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(callbackRequests);
        Assert.assertEquals(callbackRequests.size(), 1);
        CallbackRequestEntity secondExistingCallback = existingCallbackRequests.get(1);
        CallbackRequest returnedCallback = callbackRequests.get(0);
        Assert.assertEquals(secondExistingCallback.getCustomerId(), returnedCallback.getCustomerId());
        Assert.assertEquals(secondExistingCallback.getAssignedTo(), returnedCallback.getAssignedTo());
        Assert.assertEquals(secondExistingCallback.getMobileNumber(), returnedCallback.getMobileNumber());
        Assert.assertEquals(secondExistingCallback.getUpdatedBy(), returnedCallback.getUpdatedBy());
        Assert.assertEquals(secondExistingCallback.getStartDateTime().toEpochSecond(), returnedCallback.getStartDateTime().toEpochSecond());
        Assert.assertEquals(secondExistingCallback.getEndDateTime().toEpochSecond(), returnedCallback.getEndDateTime().toEpochSecond());
        Assert.assertNotNull(returnedCallback.getCreatedAt());
    }

    @Test
    public void shouldNotReturnAnyCallbackRequestsOutsideOfGivenDateTimeRange() throws Exception {
        //Given
        OffsetDateTime endDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startDateTime = endDateTime.minusMinutes(180);
        List<CallbackRequestEntity> existingCallbackRequests = List.of(
                new CallbackRequestEntity(startDateTime, endDateTime, "9004244124", "cust1", null, CallbackRequestStatus.NOT_STARTED),
                new CallbackRequestEntity(startDateTime, endDateTime.minusMinutes(10), "9004244125", "cust2", null, CallbackRequestStatus.NOT_STARTED));
        repository.saveAll(existingCallbackRequests);

        OffsetDateTime start = OffsetDateTime.parse("2023-09-08T08:29:00.000-00:00");
        OffsetDateTime end = OffsetDateTime.parse("2023-09-09T08:29:00.000-00:00");

        //When
        MvcResult mvcResult = mockMvc.perform(get("/callback-request")
                .param("startDateTime", start.toString())
                .param("endDateTime", end.toString()))
                .andExpect(status().isOk()).andReturn();

        List<CallbackRequest> callbackRequests =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(callbackRequests);
        Assert.assertEquals(callbackRequests.size(), 0);
    }

    @Test
    public void shouldReturnAllCallbackRequestsBetweenGivenDateTimeRangeForGivenStatus() throws Exception {

        //Given
        OffsetDateTime endDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startDateTime = endDateTime.minusMinutes(180);

        List<CallbackRequestEntity> existingCallbackRequests = List.of(
                new CallbackRequestEntity(startDateTime, endDateTime, "9004244124", "cust1", null, CallbackRequestStatus.NOT_STARTED),
                new CallbackRequestEntity(startDateTime, endDateTime.minusMinutes(10), "9004244125", "cust2", null, CallbackRequestStatus.CALL_CLOSED),
                new CallbackRequestEntity(startDateTime, endDateTime.minusMinutes(10), "9004244126", "cust3", null, CallbackRequestStatus.NOT_PICKED_UP));
        repository.saveAll(existingCallbackRequests);

        //When
        MvcResult mvcResult = mockMvc.perform(get("/callback-request")
                .param("startDateTime", startDateTime.toString())
                .param("endDateTime", endDateTime.toString())
                .param("status", CallbackRequestStatus.NOT_STARTED.name())
        ).andExpect(status().isOk()).andReturn();

        //Then
        List<CallbackRequest> callbackRequests =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(callbackRequests);
        Assert.assertEquals(callbackRequests.size(), 1);
        CallbackRequestEntity existingCallback = existingCallbackRequests.get(0);
        CallbackRequest returnedCallback = callbackRequests.get(0);
        Assert.assertEquals(existingCallback.getCustomerId(), returnedCallback.getCustomerId());
        Assert.assertEquals(existingCallback.getAssignedTo(), returnedCallback.getAssignedTo());
        Assert.assertEquals(existingCallback.getMobileNumber(), returnedCallback.getMobileNumber());
        Assert.assertEquals(existingCallback.getUpdatedBy(), returnedCallback.getUpdatedBy());
        Assert.assertEquals(existingCallback.getStartDateTime().toEpochSecond(), returnedCallback.getStartDateTime().toEpochSecond());
        Assert.assertEquals(existingCallback.getEndDateTime().toEpochSecond(), returnedCallback.getEndDateTime().toEpochSecond());
        Assert.assertNotNull(returnedCallback.getCreatedAt());
    }

    @Test
    public void shouldReturnAllCallbackRequestsBetweenGivenDateTimeRangeAssignedToGivenAgent() throws Exception {
        //Given
        OffsetDateTime endDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startDateTime = endDateTime.minusMinutes(180);

        List<CallbackRequestEntity> existingCallbackRequests = List.of(
                new CallbackRequestEntity(startDateTime, endDateTime, "9004244124", "cust1", "abc", CallbackRequestStatus.NOT_STARTED),
                new CallbackRequestEntity(startDateTime, endDateTime.minusMinutes(10), "9004244125", "cust2", null, CallbackRequestStatus.CALL_CLOSED),
                new CallbackRequestEntity(startDateTime, endDateTime.minusMinutes(10), "9004244126", "cust3", "xyzz", CallbackRequestStatus.NOT_PICKED_UP));
        repository.saveAll(existingCallbackRequests);

        //When
        MvcResult mvcResult = mockMvc.perform(get("/callback-request")
                .param("startDateTime", startDateTime.toString())
                .param("endDateTime", endDateTime.toString())
                .param("assignedTo", "abc")
        ).andExpect(status().isOk()).andReturn();

        //Then
        List<CallbackRequest> callbackRequests =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(callbackRequests);
        Assert.assertEquals(callbackRequests.size(), 1);
        CallbackRequestEntity existingCallback = existingCallbackRequests.get(0);
        CallbackRequest returnedCallback = callbackRequests.get(0);
        Assert.assertEquals(existingCallback.getCustomerId(), returnedCallback.getCustomerId());
        Assert.assertEquals(existingCallback.getAssignedTo(), returnedCallback.getAssignedTo());
        Assert.assertEquals(existingCallback.getMobileNumber(), returnedCallback.getMobileNumber());
        Assert.assertEquals(existingCallback.getUpdatedBy(), returnedCallback.getUpdatedBy());
        Assert.assertEquals(existingCallback.getStartDateTime().toEpochSecond(), returnedCallback.getStartDateTime().toEpochSecond());
        Assert.assertEquals(existingCallback.getEndDateTime().toEpochSecond(), returnedCallback.getEndDateTime().toEpochSecond());
        Assert.assertNotNull(returnedCallback.getCreatedAt());
    }

    @Test
    public void shouldReturnAllCallbackRequestsBetweenGivenDateTimeRangeAssignedToGivenAgentWithGivenStatus() throws Exception {
        //Given
        OffsetDateTime endDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startDateTime = endDateTime.minusMinutes(180);

        List<CallbackRequestEntity> existingCallbackRequests = List.of(
                new CallbackRequestEntity(startDateTime, endDateTime, "9004244124", "cust1", "abc", CallbackRequestStatus.NOT_STARTED),
                new CallbackRequestEntity(startDateTime, endDateTime.minusMinutes(10), "9004244125", "cust2", null, CallbackRequestStatus.CALL_CLOSED),
                new CallbackRequestEntity(startDateTime, endDateTime.minusMinutes(10), "9004244126", "cust3", "xyzz", CallbackRequestStatus.NOT_PICKED_UP));
        repository.saveAll(existingCallbackRequests);

        //When
        MvcResult mvcResult = mockMvc.perform(get("/callback-request")
                .param("startDateTime", startDateTime.toString())
                .param("endDateTime", endDateTime.toString())
                .param("assignedTo", "abc")
                .param("status", CallbackRequestStatus.NOT_STARTED.name())
        ).andExpect(status().isOk()).andReturn();

        //Then
        List<CallbackRequest> callbackRequests =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(callbackRequests);
        Assert.assertEquals(callbackRequests.size(), 1);
        CallbackRequestEntity existingCallback = existingCallbackRequests.get(0);
        CallbackRequest returnedCallback = callbackRequests.get(0);
        Assert.assertEquals(existingCallback.getCustomerId(), returnedCallback.getCustomerId());
        Assert.assertEquals(existingCallback.getAssignedTo(), returnedCallback.getAssignedTo());
        Assert.assertEquals(existingCallback.getMobileNumber(), returnedCallback.getMobileNumber());
        Assert.assertEquals(existingCallback.getUpdatedBy(), returnedCallback.getUpdatedBy());
        Assert.assertEquals(existingCallback.getStartDateTime().toEpochSecond(), returnedCallback.getStartDateTime().toEpochSecond());
        Assert.assertEquals(existingCallback.getEndDateTime().toEpochSecond(), returnedCallback.getEndDateTime().toEpochSecond());
        Assert.assertNotNull(returnedCallback.getCreatedAt());
    }

    @Test
    public void shouldReturnAllCallbackRequestsWhenNoQueryParamIsPassed() throws Exception {
        //Given
        OffsetDateTime endDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime startDateTime = endDateTime.minusMinutes(180);

        List<CallbackRequestEntity> existingCallbackRequests = List.of(
                new CallbackRequestEntity(startDateTime, endDateTime, "9004244124", "cust1", "abc", CallbackRequestStatus.NOT_STARTED),
                new CallbackRequestEntity(startDateTime, endDateTime.minusMinutes(10), "9004244125", "cust2", null, CallbackRequestStatus.CALL_CLOSED),
                new CallbackRequestEntity(startDateTime, endDateTime.minusMinutes(10), "9004244126", "cust3", "xyzz", CallbackRequestStatus.NOT_PICKED_UP));
        repository.saveAll(existingCallbackRequests);

        //When
        MvcResult mvcResult = mockMvc.perform(get("/callback-request")
        ).andExpect(status().isOk()).andReturn();

        //Then
        List<CallbackRequest> callbackRequests =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        //Then
        Assert.assertNotNull(callbackRequests);
        Assert.assertEquals(callbackRequests.size(), 3);
    }

    @Test
    public void shouldReturnBadRequestIfInvalidStatusIsPassed() throws Exception {

        //When
        MvcResult mvcResult = mockMvc.perform(get("/callback-request")
                .param("status", "CLOSE")
        ).andExpect(status().isBadRequest()).andReturn();


        System.out.println(mvcResult.getResponse().getContentAsString());

        ErrorResponse errorResponse =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        Assert.assertNotNull(errorResponse);
        Assert.assertEquals("Invalid value for parameter status", errorResponse.getErrors().get(0));
    }

    @Test
    public void shouldUpdateCallbackRequestAndReturn200() throws Exception {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String date = "2022-02-01T10:15:30+01:00";

        String date2 = "2022-02-02T10:15:30+01:00";
        OffsetDateTime startDateTime = OffsetDateTime.parse(date, dateTimeFormatter);
        OffsetDateTime endDateTime = OffsetDateTime.parse(date2, dateTimeFormatter);
        CallbackRequestEntity existingCallbackRequest = repository.save(
                new CallbackRequestEntity(
                        1L,
                        startDateTime,
                        endDateTime,
                        "98765432348",
                        "2",
                        null,
                        CallbackRequestStatus.NOT_STARTED));

        CallbackUpdateRequest request =
                new CallbackUpdateRequest(CallbackRequestStatus.NOT_STARTED, "xyz", "xyz");
        var mvcResult =
                mockMvc.perform(
                                put("/callback-request/"+existingCallbackRequest.getId())
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();
        String apiResponse = mvcResult.getResponse().getContentAsString();
        Assert.assertNotNull(apiResponse);
        Assert.assertEquals(1, repository.findAll().size());
        CallbackRequestEntity updatedEntity = repository.findAll().get(0);
        Assert.assertEquals(
                "NOT_STARTED", updatedEntity.getCallbackRequestStatus().toString());
        Assert.assertEquals("xyz", updatedEntity.getUpdatedBy());
        Assert.assertEquals("xyz", updatedEntity.getAssignedTo());
    }

    @Test
    public void shouldThrow400ErrorWhenUpdateCallbackRequestIsCalledWithInvalidId() throws Exception {

        //Given
        CallbackUpdateRequest request =
                new CallbackUpdateRequest(CallbackRequestStatus.NOT_STARTED, "xyz", "xyz");

        //When
        var mvcResult =
                mockMvc.perform(
                                put("/callback-request/1")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is4xxClientError())
                        .andReturn();

        //Then
        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals(List.of("CallbackId not available"), errorResponse.getErrors());
        Assert.assertEquals("400", errorResponse.getStatus());
    }

    public void shouldReturnBadRequestErrorIfAssignedToIsMissingInTheRequest() throws Exception {
        //Given
        CallbackUpdateRequest request =
                new CallbackUpdateRequest(CallbackRequestStatus.NOT_STARTED, "xyz", null);

        //When
        MvcResult mvcResult = mockMvc.perform(
                        put("/callback-request/1")
                                .content(mapper.writeValueAsString(request))
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
        //Given
        CallbackUpdateRequest request =
                new CallbackUpdateRequest(CallbackRequestStatus.NOT_STARTED, null, "xyz");

        //When
        MvcResult mvcResult = mockMvc.perform(
                        put("/callback-request/1")
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andReturn();
        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        //Then
        Assert.assertEquals("400", errorResponse.getStatus());
        Assert.assertEquals(List.of("updatedBy must not be blank"), errorResponse.getErrors());
    }

}
