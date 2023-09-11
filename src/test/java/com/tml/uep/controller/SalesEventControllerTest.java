package com.tml.uep.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.kafka.producers.EventMessageProducer;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.service.S3FileService;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class SalesEventControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private S3FileService s3FileService;

    @ClassRule public static WireMockRule solarApiRule = new WireMockRule(8099);

    @Autowired private SolrApiWireMockUtils mockUtils;

    private final ClassLoader classLoader = getClass().getClassLoader();

    @MockBean private EventMessageProducer eventMessageProducer;

    private static final String dPlus15ServicingBenefitsApiEndpoint =
            "/sales/events/dPlus15ServicingBenefits";

    private static final String customerAppLinksApiEndpoint = "/sales/events/customerAppLinks";

    private static final String welcomeLetterApiEndpoint = "/sales/events/welcomeLetter";

    private static final String workshopTourVideoApiEndpoint =
            "/sales/events/dMinus9WorkshopTourVideo";

    @Before
    public void setUp() {
        solarApiRule.resetRequests();
    }

    // 'Delivered to customer' use case starts

    @Test
    public void shouldProcessDeliveredToCustomerVehiclesAndSendToKafka() throws Exception {
        URL url =
                classLoader.getResource(
                        "mockResponses/deliveredToCustomer/SolrResponseWith1PageRecords.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post(dPlus15ServicingBenefitsApiEndpoint))
                .andExpect(status().isOk())
                .andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(eventMessageProducer, times(1)).send(any(String.class), any(OutboundEvent.class));
    }

    @Test
    public void throwsInternalServerIfApiDoesNotReturnDeliveredToCustomerResponseInExpectedFormat()
            throws Exception {
        URL url = classLoader.getResource("mockResponses/deliveredToCustomer/invalidResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);
        mockMvc.perform(post(dPlus15ServicingBenefitsApiEndpoint))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }
    // 'Delivered to customer' use case ends

    // Invoiced Customer use case starts

    @Test
    public void shouldProcessInvoicedVehiclesAndSendToKafka() throws Exception {
        URL url = classLoader.getResource("mockResponses/invoicedCustomer/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post(customerAppLinksApiEndpoint)).andExpect(status().isOk()).andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(eventMessageProducer, times(1)).send(any(String.class), any(OutboundEvent.class));
    }

    @Test
    public void
            throwsInternalServerErrorIfApiDoesNotReturnInvoicedCustomerResponseInExpectedFormat()
                    throws Exception {
        URL url = classLoader.getResource("mockResponses/invoicedCustomer/invalidResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);
        mockMvc.perform(post(customerAppLinksApiEndpoint))
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    // Invoiced Customer use case ends

    // 'Welcome Letter use case starts'

    @Test
    public void shouldProcessWelcomeLetterEventsAndSendToKafka() throws Exception {
        URL url = classLoader.getResource("mockResponses/welcomeLetter/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post(welcomeLetterApiEndpoint)).andExpect(status().isOk()).andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);
        verify(s3FileService, times(2)).uploadFile(any(), any());
        verify(eventMessageProducer, times(2)).send(any(String.class), any(OutboundEvent.class));
    }

    // 'Welcome Letter use case ends'

    // 'Workshop tour video use case starts'

    @Test
    public void shouldProcessWorkshopTourVideoEventsAndSendToKafka() throws Exception {
        URL url = classLoader.getResource("mockResponses/workShopTourVideo/validResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        mockMvc.perform(post(workshopTourVideoApiEndpoint)).andExpect(status().isOk()).andReturn();

        mockUtils.verifyTimesSolrApiWasCalled(1);

        verify(eventMessageProducer, times(2)).send(any(String.class), any(OutboundEvent.class));
    }

    // 'Workshop tour video use case ends'
}
