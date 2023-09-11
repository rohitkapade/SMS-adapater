package com.tml.uep.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.Call;
import com.tml.uep.model.CbslConfCallStatus;
import com.tml.uep.model.ErrorResponse;
import com.tml.uep.model.dto.cbslconfcall.ConfCallRecordingRequest;
import com.tml.uep.model.dto.cbslconfcall.ConfCallRequest;
import com.tml.uep.model.entity.CbslConfCallEntity;
import com.tml.uep.repository.CbslConfCallResponseRepository;
import com.tml.uep.service.CbslConfCallService;
import com.tml.uep.utils.DateUtils;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.tml.uep.utils.DateUtils.getYearWiseFormattedDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(AWSTestConfiguration.class)
public class CbslConfCallControllerTest {

    @ClassRule public static WireMockRule cbslConfCallApiRule = new WireMockRule(8081);

    @Autowired private SolrApiWireMockUtils mockUtils;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper mapper;

    @Autowired private CbslConfCallResponseRepository repository;

    @Autowired private CbslConfCallService service;

    private final ClassLoader classLoader = getClass().getClassLoader();

    @Value("${cbsl-conf-api.base-url}")
    private String BASE_URL;

    @Value("${cbsl-conf-api.conf-call-endpoint}")
    private String CONF_CALL_ENDPOINT;

    private final String customerMobileNumber = "919004122123";

    @Before
    public void setup() {
        cbslConfCallApiRule.resetAll();
        repository.deleteAll();
    }

    @Test
    public void shouldCallCbslConfCallApiAndReturn200() throws Exception {

        URL confCallCbslApiResponse =
                classLoader.getResource("mockResponses/cbslConfCallApi/successfulResponse.json");
        String response = Files.readString(Path.of(confCallCbslApiResponse.toURI()));
        String cbslConfCallApiEndpoint = CONF_CALL_ENDPOINT + "?phone=" + customerMobileNumber;
        mockUtils.stubInitiateCbslConfCallSuccessfully(
                cbslConfCallApiRule, cbslConfCallApiEndpoint, response);
        ConfCallRequest request = new ConfCallRequest(customerMobileNumber);

        var mvcResult =
                mockMvc.perform(
                                post("/dealer-client-conf-call")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String apiResponse = mvcResult.getResponse().getContentAsString();

        cbslConfCallApiRule.verify(1, postRequestedFor(urlEqualTo(cbslConfCallApiEndpoint)));
        Assert.assertNotNull(apiResponse);
        Assert.assertTrue(apiResponse.contains("d532b2e8e1503aed596e6bfb07ae168n"));
        Assert.assertEquals(1, repository.findAll().size());
        Assert.assertEquals("09004244790", repository.findAll().get(0).getFromPhoneNumber());
        Assert.assertEquals(
                "d532b2e8e1503aed596e6bfb07ae168n", repository.findAll().get(0).getSid());
        Assert.assertEquals(
                DateUtils.convertToOffsetDateTimeFromISTString("2022-08-23 13:10:58")
                        .toEpochSecond(),
                repository.findAll().get(0).getInitiatedStartTime().toEpochSecond());
        Assert.assertEquals(CbslConfCallStatus.INITIATED, repository.findAll().get(0).getStatus());
    }

    @Test
    public void shouldReturnBadRequestWhenPhoneNumberIsMissingInCbslConfCallRequest()
            throws Exception {

        ConfCallRequest request = new ConfCallRequest(null);
        MvcResult mvcResult =
                mockMvc.perform(
                                post("/dealer-client-conf-call")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andReturn();
        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals("400", errorResponse.getStatus());
        Assert.assertEquals(List.of("mobileNumber must not be blank"), errorResponse.getErrors());
    }

    @Test
    public void shouldUpdateAndSaveRecordingDetailsForTheInitiatedConfCall() throws Exception {

        repository.save(
                new CbslConfCallEntity(
                        new Call(
                                "d532b2e8e1503aed596e6bfb07ae168n",
                                "09004244790",
                                "2022-08-23 13:10:58",
                                "INITIATED")));
        ConfCallRecordingRequest request =
                new ConfCallRecordingRequest(
                        "09004244790",
                        "d532b2e8e1503aed596e6bfb07ae168n",
                        "120",
                        "2022-08-23 13:11:00",
                        "2022-08-23 13:13:00",
                        "recordingUrl");

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/dealer-client-conf-call/call-records")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String apiResponse = mvcResult.getResponse().getContentAsString();
        CbslConfCallEntity confCallEntity = repository.findAll().get(0);

        Assert.assertNotNull(apiResponse);
        Assert.assertEquals(1, repository.findAll().size());
        verifyCbslConfCallEntity(confCallEntity, request);
        Assert.assertEquals(
                DateUtils.convertToOffsetDateTimeFromISTString("2022-08-23 13:10:58")
                        .toEpochSecond(),
                confCallEntity.getInitiatedStartTime().toEpochSecond());
        Assert.assertEquals(CbslConfCallStatus.RECORDING_AVAILABLE, confCallEntity.getStatus());
    }

    @Test
    public void shouldSaveConfCallRecordingDetailsEvenIfSidDoesNotExistInOurDB() throws Exception {

        ConfCallRecordingRequest request =
                new ConfCallRecordingRequest(
                        "09004244790",
                        "d532b2e8e1503aed596e6bfb07ae168n",
                        "120",
                        "2022-08-23 13:11:00",
                        "2022-08-23 13:13:00",
                        "recordingUrl");

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/dealer-client-conf-call/call-records")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String apiResponse = mvcResult.getResponse().getContentAsString();
        CbslConfCallEntity confCallEntity = repository.findAll().get(0);

        Assert.assertNotNull(apiResponse);
        Assert.assertEquals(1, repository.findAll().size());
        verifyCbslConfCallEntity(confCallEntity, request);
        Assert.assertEquals(
                CbslConfCallStatus.RECORDING_AVAILABLE_WITHOUT_REFERENCE,
                confCallEntity.getStatus());
    }

    @Test
    public void shouldReturnBadRequestWhenRecordingUrlIsMissingInRequestBody() throws Exception {

        ConfCallRecordingRequest request =
                new ConfCallRecordingRequest(
                        "09004244790",
                        "d532b2e8e1503aed596e6bfb07ae168n",
                        "00:02:38",
                        "2022-08-23 13:11:00",
                        "2022-08-23 13:13:38",
                        null);

        MvcResult mvcResult =
                mockMvc.perform(
                                post("/dealer-client-conf-call/call-records")
                                        .content(mapper.writeValueAsString(request))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andReturn();
        ErrorResponse errorResponse =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ErrorResponse.class);

        Assert.assertEquals("400", errorResponse.getStatus());
        Assert.assertEquals(List.of("recordingUrl must not be blank"), errorResponse.getErrors());
    }

    @Test
    public void shouldReturnFilteredRecordsOfCbslConfCallsBasedOnStartDateEndDateAndStatusPassed()
            throws Exception {

        URL confCallCbslApiResponse =
                classLoader.getResource("mockResponses/cbslConfCallApi/successfulResponse.json");
        String response = Files.readString(Path.of(confCallCbslApiResponse.toURI()));
        String cbslConfCallApiEndpoint = CONF_CALL_ENDPOINT + "?phone=" + customerMobileNumber;
        mockUtils.stubInitiateCbslConfCallSuccessfully(
                cbslConfCallApiRule, cbslConfCallApiEndpoint, response);
        ConfCallRequest initiateCallRequest = new ConfCallRequest(customerMobileNumber);
        service.initiateCall(initiateCallRequest);

        ConfCallRecordingRequest recordingRequest1 =
                new ConfCallRecordingRequest(
                        "09004244790",
                        "d532b2e8e1503aed596e6bfb07ae168n",
                        "120",
                        "2022-08-23 13:11:00",
                        "2022-08-23 13:13:00",
                        "recordingUrl");
        service.updateRecordingDetails(recordingRequest1);

        ConfCallRecordingRequest recordingRequest2 =
                new ConfCallRecordingRequest(
                        "090042447901",
                        "d532b2e8e1503aed596e6bfb07ae168n1",
                        "120",
                        "2022-08-25 15:11:00",
                        "2022-08-25 15:13:00",
                        "recordingUrl1");
        service.updateRecordingDetails(recordingRequest2);

        MvcResult mvcResult =
                mockMvc.perform(
                                get("/dealer-client-conf-call/call-records")
                                        .param("startDate", "2022-08-22")
                                        .param("endDate", "2022-08-24")
                                        .param("status", "RECORDING_AVAILABLE"))
                        .andExpect(status().isOk())
                        .andReturn();

        List<CbslConfCallEntity> entity =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        Assert.assertEquals(entity.size(), 1);
        Assert.assertEquals("09004244790", entity.get(0).getFromPhoneNumber());
        Assert.assertEquals("d532b2e8e1503aed596e6bfb07ae168n", entity.get(0).getSid());
        Assert.assertNotNull(entity.get(0).getInitiatedStartTime());
        Assert.assertEquals("120", entity.get(0).getDialCallDuration());
        Assert.assertEquals("recordingUrl", entity.get(0).getRecordingUrl());
        Assert.assertEquals(CbslConfCallStatus.RECORDING_AVAILABLE, entity.get(0).getStatus());
    }

    @Test
    public void shouldReturnFilteredRecordsOfCbslConfCallsBasedOnStatusPassed() throws Exception {

        OffsetDateTime dateTime = OffsetDateTime.now();
        OffsetDateTime endDateTime = dateTime.plusMinutes(2);
        ConfCallRecordingRequest request =
                new ConfCallRecordingRequest(
                        "09004244790",
                        "d532b2e8e1503aed596e6bfb07ae168n",
                        "120",
                        getYearWiseFormattedDate(dateTime),
                        getYearWiseFormattedDate(endDateTime),
                        "recordingUrl");
        service.updateRecordingDetails(request);

        ConfCallRecordingRequest request1 =
                new ConfCallRecordingRequest(
                        "09004244790",
                        "d532b2e8e1503aed596e6bfb07ae168n1",
                        "1201",
                        "2022-08-23 15:11:00",
                        "2022-08-23 15:13:38",
                        "recordingUrl1");
        service.updateRecordingDetails(request1);

        MvcResult mvcResult =
                mockMvc.perform(
                                get("/dealer-client-conf-call/call-records")
                                        .param("status", "RECORDING_AVAILABLE_WITHOUT_REFERENCE"))
                        .andExpect(status().isOk())
                        .andReturn();

        List<CbslConfCallEntity> entity =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        Assert.assertEquals(entity.size(), 2);
        Assert.assertEquals(
                CbslConfCallStatus.RECORDING_AVAILABLE_WITHOUT_REFERENCE,
                entity.get(0).getStatus());
        Assert.assertEquals(
                CbslConfCallStatus.RECORDING_AVAILABLE_WITHOUT_REFERENCE,
                entity.get(1).getStatus());
        verifyCbslConfCallEntity(entity.get(0), request);
        verifyCbslConfCallEntity(entity.get(1), request1);
    }

    @Test
    public void shouldReturnAllRecordsOfCbslConfCallsWhenNoParamsArePassed() throws Exception {

        OffsetDateTime dateTime = OffsetDateTime.now();
        OffsetDateTime endDateTime = dateTime.plusMinutes(2);
        ConfCallRecordingRequest request =
                new ConfCallRecordingRequest(
                        "09004244790",
                        "d532b2e8e1503aed596e6bfb07ae168n",
                        "120",
                        getYearWiseFormattedDate(dateTime),
                        getYearWiseFormattedDate(endDateTime),
                        "recordingUrl");
        service.updateRecordingDetails(request);

        MvcResult mvcResult =
                mockMvc.perform(get("/dealer-client-conf-call/call-records"))
                        .andExpect(status().isOk())
                        .andReturn();

        List<CbslConfCallEntity> entity =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        Assert.assertEquals(entity.size(), 1);
        Assert.assertEquals(
                CbslConfCallStatus.RECORDING_AVAILABLE_WITHOUT_REFERENCE,
                entity.get(0).getStatus());
        verifyCbslConfCallEntity(entity.get(0), request);
    }

    @Test
    public void shouldReturnFilteredRecordsOfCbslConfCallsBasedOnStartDateAndEndDatePassed()
            throws Exception {

        OffsetDateTime dateTime = OffsetDateTime.now();
        OffsetDateTime endDateTime = dateTime.plusMinutes(2);
        ConfCallRecordingRequest request =
                new ConfCallRecordingRequest(
                        "09004244790",
                        "d532b2e8e1503aed596e6bfb07ae168n",
                        "120",
                        getYearWiseFormattedDate(dateTime),
                        getYearWiseFormattedDate(endDateTime),
                        "recordingUrl");
        service.updateRecordingDetails(request);

        ConfCallRecordingRequest request1 =
                new ConfCallRecordingRequest(
                        "09004244790",
                        "d532b2e8e1503aed596e6bfb07ae168n1",
                        "1201",
                        "2022-08-23 15:11:00",
                        "2022-08-23 15:13:38",
                        "recordingUrl1");
        service.updateRecordingDetails(request1);

        MvcResult mvcResult =
                mockMvc.perform(
                                get("/dealer-client-conf-call/call-records")
                                        .param("startDate", "2022-08-23")
                                        .param("endDate", "2022-08-24"))
                        .andExpect(status().isOk())
                        .andReturn();

        List<CbslConfCallEntity> entity =
                mapper.readValue(
                        mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

        Assert.assertEquals(entity.size(), 1);
        Assert.assertEquals(
                CbslConfCallStatus.RECORDING_AVAILABLE_WITHOUT_REFERENCE,
                entity.get(0).getStatus());
        verifyCbslConfCallEntity(entity.get(0), request1);
    }

    private void verifyCbslConfCallEntity(
            CbslConfCallEntity confCallEntity, ConfCallRecordingRequest request) {
        Assert.assertEquals(request.getCallFrom(), confCallEntity.getFromPhoneNumber());
        Assert.assertEquals(request.getSid(), confCallEntity.getSid());
        Assert.assertEquals(request.getDialCallDuration(), confCallEntity.getDialCallDuration());
        Assert.assertEquals(request.getRecordingUrl(), confCallEntity.getRecordingUrl());
        Assert.assertEquals(
                DateUtils.convertToOffsetDateTimeFromISTString(request.getStartTime())
                        .toEpochSecond(),
                confCallEntity.getStartTime().toEpochSecond());
        Assert.assertEquals(
                DateUtils.convertToOffsetDateTimeFromISTString(request.getEndTime())
                        .toEpochSecond(),
                confCallEntity.getEndTime().toEpochSecond());
    }
}
