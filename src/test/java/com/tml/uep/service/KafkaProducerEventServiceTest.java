package com.tml.uep.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.kafka.producers.ErrorEventsProducer;
import com.tml.uep.kafka.producers.EventMessageProducer;
import com.tml.uep.model.Event;
import com.tml.uep.model.ServicingBenefitsEvent;
import com.tml.uep.model.WelcomeLetterEvent;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.solr_api.EventsFetcherSolrService;
import com.tml.uep.solr_api.dto.SolrApiResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Import(AWSTestConfiguration.class)
public class KafkaProducerEventServiceTest {

    @Autowired private EventsFetcherSolrService eventsFetcherSolrService;

    @MockBean private EventMessageProducer eventMessageProducer;

    @MockBean private ErrorEventsProducer errorEventsProducer;

    @Autowired private KafkaProducerEventService producerService;

    @ClassRule public static WireMockRule solarApiRule = new WireMockRule(8099);

    @Autowired private SolrApiWireMockUtils mockUtils;

    @Value("${solr-api.auth-token}")
    private String solarApiAuthHeader;

    @Value("${solr-api.events-endpoint}")
    private String eventsEndpoint;

    @Before
    public void setUp() {
        solarApiRule.resetRequests();
    }

    private ClassLoader classLoader = getClass().getClassLoader();

    private static final OffsetDateTime fromDate =
            OffsetDateTime.of(2021, 9, 15, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime toDate =
            OffsetDateTime.of(2021, 9, 16, 0, 0, 0, 0, ZoneOffset.UTC);

    @Test
    public void shouldSendAllMessagesToKafkaWhenThereIsOnlyOnePageOfRecords()
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {

        URL url =
                classLoader.getResource(
                        "mockResponses/deliveredToCustomer/SolrResponseWith1PageRecords.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        producerService.sendAllEventMessagesToKafka(
                Event.SERVICING_BENEFITS,
                fromDate,
                toDate,
                new ParameterizedTypeReference<SolrApiResponse<ServicingBenefitsEvent>>() {});

        mockUtils.verifyTimesSolrApiWasCalled(1);
        verify(eventMessageProducer, times(1)).send(any(String.class), any(OutboundEvent.class));
    }

    @Test
    public void shouldHandleErrorEventsAndValidEventsAppropriately()
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {

        URL url = classLoader.getResource("mockResponses/welcomeLetter/missingFields.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        producerService.sendAllEventMessagesToKafka(
                Event.WELCOME_LETTER,
                fromDate,
                toDate,
                new ParameterizedTypeReference<SolrApiResponse<WelcomeLetterEvent>>() {});

        mockUtils.verifyTimesSolrApiWasCalled(1);
        // TODO : to remove comments from below lines once welcome letter is fully done.
        //        verify(eventMessageProducer, times(1))
        //                .send(any(String.class), any(OutboundEvent.class)); // valid event
        //        verify(errorEventsProducer, times(1))
        //                .send(any(String.class), any(OutboundEvent.class)); // error event
    }

    //    @Test
    //    public void shouldSendAllMessagesToKafkaWhenThereAreMultiplePagesOfRecords()
    //            throws ExecutionException, InterruptedException, URISyntaxException, IOException {
    //
    //        URL url =
    //                classLoader.getResource(
    //
    // "mockResponses/deliveredToCustomer/SolrApiValidFirstPageResponse.json");
    //        String firstResponse = Files.readString(Path.of(url.toURI()));
    //
    //        url =
    //                classLoader.getResource(
    //
    // "mockResponses/deliveredToCustomer/SolrApiValidSecondPageResponse.json");
    //        String secondResponse = Files.readString(Path.of(url.toURI()));
    //
    //        mockUtils.stubSolarApiResponseForScenario(
    //                firstResponse,
    //                "deliveredToCustomerApiCall",
    //                STARTED,
    //                "deliveredToCustomerSecondApiCall",
    //                solarApiRule);
    //        mockUtils.stubSolarApiResponseForScenario(
    //                secondResponse,
    //                "deliveredToCustomerApiCall",
    //                "deliveredToCustomerSecondApiCall",
    //                "deliveredToCustomerSecondApiCall",
    //                solarApiRule);
    //
    //        producerService.sendAllEventMessagesToKafka(
    //                Event.SERVICING_BENEFITS,
    //                fromDate,
    //                toDate,
    //                new ParameterizedTypeReference<SolrApiResponse<ServicingBenefitsEvent>>() {});
    //
    //        mockUtils.verifyTimesSolrApiWasCalled(2);
    //        verify(eventMessageProducer, times(3)).send(any(String.class),
    // any(OutboundEvent.class));
    //    }

    //  @Test
    //  public void
    // shouldSendAllMessagesToKafkaWhenThereAreMultiplePagesOfRecordsForServicesScheduled()
    //      throws ExecutionException, InterruptedException, URISyntaxException, IOException {
    //
    //    URL url =
    //
    // classLoader.getResource("mockResponses/scheduledServiceEvents/validFirstPageResponse.json");
    //    String firstResponse = Files.readString(Path.of(url.toURI()));
    //
    //    url =
    //        classLoader.getResource(
    //            "mockResponses/scheduledServiceEvents/validSecondPageResponse.json");
    //    String secondResponse = Files.readString(Path.of(url.toURI()));
    //
    //    mockUtils.stubSolarApiResponseForScenario(
    //        firstResponse, "scheduledServiceEventsApiCall", STARTED,
    // "scheduledServiceEventsSecondApiCall", solarApiRule);
    //    mockUtils.stubSolarApiResponseForScenario(
    //        secondResponse,
    //        "scheduledServiceEventsApiCall",
    //        "scheduledServiceEventsSecondApiCall",
    //        "scheduledServiceEventsSecondApiCall",
    //        solarApiRule);
    //
    //    producerService.sendAllEventMessagesToKafka(
    //        Event.SERVICE_APPOINTMENT,
    //        fromDate,
    //        toDate,
    //        new ParameterizedTypeReference<SolrApiResponse<ServiceScheduledEvent>>() {});
    //
    //    mockUtils.verifyTimesSolrApiWasCalled(2);
    //    verify(eventMessageProducer, times(6)).send(any(String.class), any(OutboundEvent.class));
    //  }
}
