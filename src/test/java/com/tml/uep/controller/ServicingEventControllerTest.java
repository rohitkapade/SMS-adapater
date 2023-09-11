package com.tml.uep.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.kafka.producers.ErrorEventsProducer;
import com.tml.uep.kafka.producers.EventMessageProducer;
import com.tml.uep.model.BusinessUnit;
import com.tml.uep.model.Event;
import com.tml.uep.model.kafka.MessageType;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.service.ServicingEventService;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(AWSTestConfiguration.class)
public class ServicingEventControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ServicingEventService servicingEventService;

    @ClassRule public static WireMockRule solarApiRule = new WireMockRule(8099);

    @Autowired private SolrApiWireMockUtils mockUtils;

    @Captor ArgumentCaptor<OutboundEvent> outboundEventArgumentCaptor;

    @Before
    public void setUp() {
        solarApiRule.resetRequests();
    }

    private final ClassLoader classLoader = getClass().getClassLoader();

    @MockBean private EventMessageProducer eventMessageProducer;

    @MockBean private ErrorEventsProducer errorEventsProducer;

    private final String scheduledServiceEventsApiUrl =
            "/servicing/events/dMinus1ServicingScheduled";

    @Test
    public void shouldCallSolrApiAndProduceScheduledEventsOnKafka() throws Exception {
        URL url =
                classLoader.getResource("mockResponses/scheduledServiceEvents/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post(scheduledServiceEventsApiUrl)).andExpect(status().isOk()).andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(eventMessageProducer, times(1)).send(any(String.class), any(OutboundEvent.class));
    }

    @Test
    public void throwsInternalServerIfApiDoesNotReturnResponseInExpectedFormat() throws Exception {
        URL url =
                classLoader.getResource(
                        "mockResponses/scheduledServiceEvents/invalidResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);
        mockMvc.perform(post(scheduledServiceEventsApiUrl))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    // Ready for delivery use case starts

    private static String readyForDeliveryApiUrl = "/servicing/events/readyForDelivery";

    @Test
    public void shouldProcessReadyForDeliveryVehiclesAndSendToKafka() throws Exception {
        URL url =
                classLoader.getResource("mockResponses/readyForDeliveryEvents/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post(readyForDeliveryApiUrl)).andExpect(status().isOk()).andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(eventMessageProducer, times(2)).send(any(String.class), any(OutboundEvent.class));
    }

    @Test
    public void
            shouldWriteToErrorKafkaTopicForReadyForDeliveryVehiclesWhenRequiredFieldsAreMissing()
                    throws Exception {
        URL url =
                classLoader.getResource(
                        "mockResponses/readyForDeliveryEvents/missingFieldsResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post(readyForDeliveryApiUrl)).andExpect(status().isOk()).andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(errorEventsProducer, times(2)).send(any(String.class), any(OutboundEvent.class));
    }

    @Test
    public void throwsInternalServerIfApiDoesNotReturnReadyForDeliveryResponseInExpectedFormat()
            throws Exception {
        URL url =
                classLoader.getResource(
                        "mockResponses/readyForDeliveryEvents/invalidResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);
        mockMvc.perform(post(readyForDeliveryApiUrl))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }
    // Ready for delivery use case ends

    @Test
    public void shouldCallSolrApiAndProduceNextServiceReminderEventsOnKafka() throws Exception {
        URL url = classLoader.getResource("mockResponses/nextServiceReminder/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post("/servicing/events/nextServiceReminder/9"))
                .andExpect(status().isOk())
                .andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(eventMessageProducer, times(2)).send(any(String.class), any(OutboundEvent.class));
    }

    @Test
    public void
            shouldWriteToErrorKafkaTopicForNextServiceReminderVehiclesWhenRequiredFieldsAreMissing()
                    throws Exception {
        URL url = classLoader.getResource("mockResponses/nextServiceReminder/invalidResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post("/servicing/events/nextServiceReminder/9"))
                .andExpect(status().isOk())
                .andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(errorEventsProducer, times(2)).send(any(String.class), any(OutboundEvent.class));
    }

    // Instant feedback use case starts
    @Test
    public void shouldCallSolrApiAndInstantFeedbackEventsOnKafka() throws Exception {
        URL url =
                classLoader.getResource("mockResponses/serviceInstantFeedback/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post("/servicing/events/serviceInstantFeedback"))
                .andExpect(status().isOk())
                .andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(eventMessageProducer, times(1)).send(any(String.class), any(OutboundEvent.class));
    }

    @Test
    public void throwsInternalServerErrorIfApiDoesNotReturnInstantFeedbackResponseInExpectedFormat()
            throws Exception {
        URL url =
                classLoader.getResource(
                        "mockResponses/serviceInstantFeedback/invalidResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);
        mockMvc.perform(post("/servicing/events/serviceInstantFeedback"))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    // Instant feedback use case ends

    // D plus 3 feedback (Service Quality feedback) use case starts
    @Test
    public void shouldCallSolrApiAndDPlus3QualityFeedbackEventsOnKafka() throws Exception {
        URL url =
                classLoader.getResource("mockResponses/serviceQualityFeedback/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post("/servicing/events/dPlus3ServiceRequestClosed"))
                .andExpect(status().isOk())
                .andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(eventMessageProducer, times(1)).send(any(String.class), any(OutboundEvent.class));
    }

    @Test
    public void
            throwsInternalServerErrorIfApiDoesNotReturnDPlus3QualityFeedbackResponseInExpectedFormat()
                    throws Exception {
        URL url =
                classLoader.getResource(
                        "mockResponses/serviceQualityFeedback/invalidResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);
        mockMvc.perform(post("/servicing/events/dPlus3ServiceRequestClosed"))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    // D plus 3 feedback (Service Quality feedback) use case ends

    // Pickup drop (Service Scheduled) use case starts
    @Test
    public void shouldCallSolrApiAndPushPickDropServicingScheduledEventsOnKafka() throws Exception {
        URL url =
                classLoader.getResource(
                        "mockResponses/pickDropScheduledServiceEvents/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post("/servicing/events/pickupDropServicingScheduled"))
                .andExpect(status().isOk())
                .andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(eventMessageProducer, times(1)).send(any(String.class), any(OutboundEvent.class));
    }

    @Test
    public void throwsInternalServerErrorIfApiDoesNotReturnPickDropServiceResponseInExpectedFormat()
            throws Exception {
        URL url =
                classLoader.getResource(
                        "mockResponses/pickDropScheduledServiceEvents/invalidResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);
        mockMvc.perform(post("/servicing/events/pickupDropServicingScheduled"))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    // Pickup drop (Service Scheduled) use case ends

    // service_appointment_before_dealership_confirmation use case starts
    @Test
    public void shouldCallSolrApiAndPushAppointmentBeforeConfirmationEventsOnKafka()
            throws Exception {
        URL url =
                classLoader.getResource(
                        "mockResponses/appointmentBeforeDealerConfirmation/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post("/servicing/events/serviceAppointmentBeforeDealershipConfirmation"))
                .andExpect(status().isOk())
                .andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(eventMessageProducer, times(1))
                .send(any(String.class), outboundEventArgumentCaptor.capture());
        Assert.assertEquals("9821945706", outboundEventArgumentCaptor.getValue().getReceiverId());
        Assert.assertEquals(
                Event.SERVICE_APPOINTMENT_BEFORE_DEALERSHIP_CONFIRMATION,
                outboundEventArgumentCaptor.getValue().getEventType());
        Assert.assertEquals(
                BusinessUnit.PV, outboundEventArgumentCaptor.getValue().getBusinessUnit());
        Assert.assertEquals(null, outboundEventArgumentCaptor.getValue().getFileUrl());
        Assert.assertEquals(
                MessageType.TEXT, outboundEventArgumentCaptor.getValue().getMessageType());
    }

    // service_appointment_before_dealership_confirmation use case ends

}
