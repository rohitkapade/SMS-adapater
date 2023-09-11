package com.tml.uep.solr_api;

import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tml.uep.SolrApiWireMockUtils;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.Event;
import com.tml.uep.model.ServicingBenefitsEvent;
import com.tml.uep.solr_api.dto.SolrApiResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Import(AWSTestConfiguration.class)
public class EventsFetcherSolrServiceTests {

    @ClassRule public static WireMockRule solarApiRule = new WireMockRule(8099);

    @Autowired private EventsFetcherSolrService service;

    @Autowired private SolrApiWireMockUtils mockUtils;

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
    public void shouldReturnSolarApiResponseWhenThereIsOnlyOnePageOfRecords()
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {
        URL url =
                classLoader.getResource(
                        "mockResponses/deliveredToCustomer/SolrResponseWith1PageRecords.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        List<CompletableFuture<SolrApiResponse<ServicingBenefitsEvent>>> responses =
                service.getRecordsForEvent(
                        Event.SERVICING_BENEFITS,
                        fromDate,
                        toDate,
                        new ParameterizedTypeReference<>() {});
        responses.forEach(
                response -> {
                    SolrApiResponse<ServicingBenefitsEvent> futureResponse = null;
                    try {
                        futureResponse = response.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Assert.fail(
                                "InterruptedException happened while getting the result of completable future");
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        Assert.fail(
                                "ExecutionException happened while getting the result of completable future");
                    }
                    mockUtils.verifyTimesSolrApiWasCalled(1);
                    List<ServicingBenefitsEvent> data = futureResponse.getData();
                    Assert.assertNotNull(data);
                    Assert.assertEquals(1, data.size());
                    Assert.assertEquals("7980883043", data.get(0).getMobileNumber());
                });
    }

    @Test
    public void shouldReturnSolarApiResponseWhenThereAreNoRecords()
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {
        URL url =
                classLoader.getResource(
                        "mockResponses/deliveredToCustomer/SolrResponseWithNoRecords.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        List<CompletableFuture<SolrApiResponse<ServicingBenefitsEvent>>> responses =
                service.getRecordsForEvent(
                        Event.SERVICING_BENEFITS,
                        fromDate,
                        toDate,
                        new ParameterizedTypeReference<>() {});
        responses.forEach(
                response -> {
                    SolrApiResponse<ServicingBenefitsEvent> futureResponse = null;
                    try {
                        futureResponse = response.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Assert.fail(
                                "InterruptedException happened while getting the result of completable future");
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        Assert.fail(
                                "ExecutionException happened while getting the result of completable future");
                    }
                    mockUtils.verifyTimesSolrApiWasCalled(1);
                    List<ServicingBenefitsEvent> data = futureResponse.getData();
                    Assert.assertEquals(0, futureResponse.getTotalRecordCount());
                    Assert.assertEquals(0, data.size());
                });
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowExceptionWhenSolarApiResponseDoesntHaveDataNode()
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {

        URL url =
                classLoader.getResource(
                        "mockResponses/deliveredToCustomer/SolrResponseInvalidWithoutData.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        service.getRecordsForEvent(
                Event.SERVICING_BENEFITS,
                fromDate,
                toDate,
                new ParameterizedTypeReference<SolrApiResponse<ServicingBenefitsEvent>>() {});
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowExceptionWhenSolarApiResponseHasInvalidJsonData()
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {

        URL url =
                classLoader.getResource(
                        "mockResponses/deliveredToCustomer/SolrResponseInvalidJsonResponse.json");
        String apiResponse = Files.readString(Path.of(url.toURI()));
        mockUtils.stubSolarApiResponse(apiResponse, solarApiRule);

        service.getRecordsForEvent(
                Event.SERVICING_BENEFITS,
                fromDate,
                toDate,
                new ParameterizedTypeReference<SolrApiResponse<ServicingBenefitsEvent>>() {});
    }

    @Test
    public void shouldReturnSolarApiResponseWhenThereIsAreMultiplePagesOfRecords()
            throws ExecutionException, InterruptedException, URISyntaxException, IOException {

        URL url =
                classLoader.getResource(
                        "mockResponses/deliveredToCustomer/SolrApiValidFirstPageResponse.json");
        String firstResponse = Files.readString(Path.of(url.toURI()));

        url =
                classLoader.getResource(
                        "mockResponses/deliveredToCustomer/SolrApiValidSecondPageResponse.json");
        String secondResponse = Files.readString(Path.of(url.toURI()));

        mockUtils.stubSolarApiResponseForScenario(
                firstResponse, "ApiCall", STARTED, "SecondApiCall", solarApiRule);
        mockUtils.stubSolarApiResponseForScenario(
                secondResponse, "ApiCall", "SecondApiCall", "SecondApiCall", solarApiRule);

        int pageSize = 2;
        List<CompletableFuture<SolrApiResponse<ServicingBenefitsEvent>>> responses =
                service.getRecordsForEvent(
                        Event.SERVICING_BENEFITS,
                        fromDate,
                        toDate,
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(pageSize, responses.size());
        SolrApiResponse<ServicingBenefitsEvent> result = responses.get(0).get();
        List<ServicingBenefitsEvent> data = result.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals(pageSize, data.size());
        Assert.assertEquals("7980883043", data.get(0).getMobileNumber());
        Assert.assertEquals("9811071688", data.get(1).getMobileNumber());

        result = responses.get(1).get();
        data = result.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());
        Assert.assertEquals("9001071688", data.get(0).getMobileNumber());

        mockUtils.verifyTimesSolrApiWasCalled(2);
    }
}
