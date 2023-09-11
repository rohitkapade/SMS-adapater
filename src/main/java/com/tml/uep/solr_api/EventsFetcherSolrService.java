package com.tml.uep.solr_api;

import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.Event;
import com.tml.uep.solr_api.dto.SolrApiPayload;
import com.tml.uep.solr_api.dto.SolrApiResponse;
import com.tml.uep.utils.DateUtils;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class EventsFetcherSolrService {

    @Value("${solr-api.events-endpoint}")
    private String eventsEndpoint;

    @Value("${solr-api.page-size}")
    private int pageSize;

    @Autowired
    @Qualifier("solrRestTemplate")
    private RestTemplate restTemplate;

    public <T> List<CompletableFuture<SolrApiResponse<T>>> getRecordsForEvent(
            Event eventType,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            ParameterizedTypeReference<SolrApiResponse<T>> responseType)
            throws ExecutionException, InterruptedException {

        int offset = 0;
        // Make first api call which will tell us the total # of records available and
        // the number of times the api needs to be invoked
        ArrayList<CompletableFuture<SolrApiResponse<T>>> responses = new ArrayList<>();
        CompletableFuture<SolrApiResponse<T>> apiResponseCallback =
                getDataFromSolarApi(eventType, fromDate, toDate, offset, responseType);
        SolrApiResponse<T> apiResponse = apiResponseCallback.get();
        responses.add(apiResponseCallback);

        int totalMatchingRecordsCount = apiResponse.getTotalRecordCount();

        int remainingRecordsToBeFetched = totalMatchingRecordsCount - pageSize;

        while (remainingRecordsToBeFetched > 0) {
            offset = offset + pageSize;
            CompletableFuture<SolrApiResponse<T>> subsequentResponse =
                    getDataFromSolarApi(eventType, fromDate, toDate, offset, responseType);
            responses.add(subsequentResponse);
            remainingRecordsToBeFetched = remainingRecordsToBeFetched - pageSize;
        }
        return responses;
    }

    private <T> CompletableFuture<SolrApiResponse<T>> getDataFromSolarApi(
            Event eventType,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            int offset,
            ParameterizedTypeReference<SolrApiResponse<T>> responseType) {

        return CompletableFuture.supplyAsync(
                () -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.put(
                            HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));

                    String formattedFromDate = DateUtils.getFormattedDate(fromDate);
                    String formattedToDate = DateUtils.getFormattedDate(toDate);
                    String entity = eventType.toString();

                    HttpEntity<SolrApiPayload> requestEntity =
                            new HttpEntity<>(
                                    new SolrApiPayload(
                                            formattedFromDate,
                                            formattedToDate,
                                            entity,
                                            offset,
                                            pageSize),
                                    headers);
                    log.info(
                            "Invoking Solr api for event {} with offset {} for date range {} - {}",
                            eventType.name(),
                            offset,
                            formattedFromDate,
                            formattedToDate);
                    ResponseEntity<SolrApiResponse<T>> responseEntity =
                            restTemplate.exchange(
                                    eventsEndpoint, HttpMethod.POST, requestEntity, responseType);

                    SolrApiResponse<T> response = responseEntity.getBody();
                    log.info("Solr Api Response - {}", response);
                    if (response == null || response.getData() == null) {
                        throw new ExternalSystemException(
                                "Solar api response did not contain data field or itself was empty");
                    }

                    return response;
                });
    }
}
