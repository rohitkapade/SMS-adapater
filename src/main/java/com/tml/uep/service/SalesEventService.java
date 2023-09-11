package com.tml.uep.service;

import com.tml.uep.model.*;
import com.tml.uep.solr_api.EventsFetcherSolrService;
import com.tml.uep.solr_api.dto.SolrApiResponse;
import com.tml.uep.utils.DateUtils;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SalesEventService {

    @Autowired private EventProcessingService eventProcessingService;

    @Autowired private EventsFetcherSolrService eventsFetcherSolrService;

    @Autowired private FileEventService fileEventService;

    /*
     * If date range is not passed, fetches event from last event fetched date stored in db.
     * If last event fetched date is not found in db, then fetches event for 15th day before today
     * */
    public void getAndPublishDPlus15ServicingBenefitsEvents(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        ParameterizedTypeReference<SolrApiResponse<ServicingBenefitsEvent>> responseType =
                new ParameterizedTypeReference<>() {};
        OffsetDateTime todaysStartTime = DateUtils.getTodaysStartTime();
        OffsetDateTime defaultRangeStart = todaysStartTime.minusDays(15);
        DateRange dateRangeIfNull =
                new DateRange(defaultRangeStart, DateUtils.getEndDateTimeOf(defaultRangeStart));
        eventProcessingService.getAndProcessEventWithHistory(
                responseType, Event.SERVICING_BENEFITS, fromDateTime, toDateTime, dateRangeIfNull);
    }

    public void getAndPublishCustomerAppLinksEvents(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        ParameterizedTypeReference<SolrApiResponse<CustomerAppLinksEvent>> responseType =
                new ParameterizedTypeReference<>() {};
        DateRange dateRangeIfNull =
                new DateRange(DateUtils.getTodaysStartTime(), OffsetDateTime.now(ZoneOffset.UTC));
        eventProcessingService.getAndProcessEventWithHistory(
                responseType, Event.CUSTOMER_APP_LINKS, fromDateTime, toDateTime, dateRangeIfNull);
    }

    public <T extends OutboundEventConverter> void getAndPublishWelcomeLetterEvents(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        DateRange dateRange =
                eventProcessingService.getDateRangeForShortTermIfNull(
                        fromDateTime, toDateTime, Event.WELCOME_LETTER);
        ParameterizedTypeReference<SolrApiResponse<WelcomeLetterEvent>> responseType =
                new ParameterizedTypeReference<>() {};
        List<CompletableFuture<SolrApiResponse<WelcomeLetterEvent>>> recordsForEvent =
                eventsFetcherSolrService.getRecordsForEvent(
                        Event.WELCOME_LETTER,
                        dateRange.getFromDateTime(),
                        dateRange.getToDateTime(),
                        responseType);
        recordsForEvent.forEach(
                response -> {
                    response.thenAccept(
                            records -> {
                                final List<WelcomeLetterEvent> data = records.getData();
                                data.forEach(
                                        welcomeLetterEvent -> {
                                            if (!welcomeLetterEvent.isValid()) {
                                                log.info(
                                                        "ignoring welcome letter event that is not valid for chassis num: {}",
                                                        welcomeLetterEvent.getId());
                                                return;
                                            }
                                            try {
                                                fileEventService
                                                        .generatePDFFromTemplateAndSendToKafka(
                                                                "welcome-letter-template.html",
                                                                welcomeLetterEvent
                                                                        .getPlaceholderMap(),
                                                                welcomeLetterEvent,
                                                                welcomeLetterEvent
                                                                        .getMobileNumber());
                                            } catch (Exception e) {
                                                log.error(e.getMessage());
                                                e.printStackTrace();
                                            }
                                        });
                                eventProcessingService.saveEventRetrievalHistory(
                                        Event.WELCOME_LETTER, dateRange.getToDateTime());
                            });
                });
    }

    public void getAndPublishDMinus9WorkshopTourVideo(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        ParameterizedTypeReference<SolrApiResponse<WorkshopTourVideoEvent>> responseType =
                new ParameterizedTypeReference<>() {};
        OffsetDateTime startTime = DateUtils.getTodaysStartTime().plusDays(9);
        DateRange dateRangeIfNull = new DateRange(startTime, DateUtils.getEndDateTimeOf(startTime));
        eventProcessingService.getAndProcessEventWithoutHistory(
                responseType, Event.WORKSHOP_TOUR_VIDEO, fromDateTime, toDateTime, dateRangeIfNull);
    }
}
