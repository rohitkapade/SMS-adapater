package com.tml.uep.service;

import com.tml.uep.model.DateRange;
import com.tml.uep.model.Event;
import com.tml.uep.model.OutboundEventConverter;
import com.tml.uep.solr_api.dto.SolrApiResponse;
import com.tml.uep.utils.DateUtils;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventProcessingService {

    @Autowired private KafkaProducerEventService kafkaEventProducer;

    @Autowired private EventDataRetrievalHistoryService historyService;

    private static final String DEFAULT_TIME_RANGE_MSG =
            "Either fromDateTime: {} or toDateTime: {} or both were not passed. Using default date range";
    private static final String ACTUAL_TIME_RANGE_MSG =
            "Fetching {} events for time interval {} to {}";

    public <T extends OutboundEventConverter> void getAndProcessShortTermEvent(
            ParameterizedTypeReference<SolrApiResponse<T>> responseType,
            Event event,
            OffsetDateTime fromDateTime,
            OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        DateRange dateRange = getDateRangeForShortTermIfNull(fromDateTime, toDateTime, event);
        logInfoAndProduceToKafka(
                responseType, event, dateRange.getFromDateTime(), dateRange.getToDateTime());
    }

    public DateRange getDateRangeForShortTermIfNull(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime, Event event) {
        if (fromDateTime == null || toDateTime == null) {
            log.info(DEFAULT_TIME_RANGE_MSG, fromDateTime, toDateTime);
            Optional<OffsetDateTime> historyOptional = historyService.getLastFetchedDateTime(event);
            fromDateTime = historyOptional.orElse(DateUtils.getTodaysStartTime());
            toDateTime = OffsetDateTime.now(ZoneOffset.UTC);
        }
        return new DateRange(fromDateTime, toDateTime);
    }

    public <T extends OutboundEventConverter> void getAndProcessEventWithHistory(
            ParameterizedTypeReference<SolrApiResponse<T>> responseType,
            Event event,
            OffsetDateTime fromDateTime,
            OffsetDateTime toDateTime,
            DateRange dateRangeIfNull)
            throws ExecutionException, InterruptedException {
        if (fromDateTime == null || toDateTime == null) {
            log.info(DEFAULT_TIME_RANGE_MSG, fromDateTime, toDateTime);
            Optional<OffsetDateTime> historyOptional = historyService.getLastFetchedDateTime(event);
            fromDateTime = historyOptional.orElse(dateRangeIfNull.getFromDateTime());
            toDateTime = dateRangeIfNull.getToDateTime();
        }
        logInfoAndProduceToKafka(responseType, event, fromDateTime, toDateTime);
    }

    public <T extends OutboundEventConverter> void getAndProcessEventWithoutHistory(
            ParameterizedTypeReference<SolrApiResponse<T>> responseType,
            Event event,
            OffsetDateTime fromDateTime,
            OffsetDateTime toDateTime,
            DateRange dateRangeIfNull)
            throws ExecutionException, InterruptedException {
        if (fromDateTime == null || toDateTime == null) {
            log.info(DEFAULT_TIME_RANGE_MSG, fromDateTime, toDateTime);
            fromDateTime = dateRangeIfNull.getFromDateTime();
            toDateTime = dateRangeIfNull.getToDateTime();
        }
        logInfoAndProduceToKafka(responseType, event, fromDateTime, toDateTime);
    }

    public <T extends OutboundEventConverter> void logInfoAndProduceToKafka(
            ParameterizedTypeReference<SolrApiResponse<T>> responseType,
            Event event,
            OffsetDateTime fromDateTime,
            OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        log.info(ACTUAL_TIME_RANGE_MSG, event.name(), fromDateTime, toDateTime);
        kafkaEventProducer.sendAllEventMessagesToKafka(
                event, fromDateTime, toDateTime, responseType);
    }

    public void saveEventRetrievalHistory(Event event, OffsetDateTime lastUpdatedTime) {
        historyService.saveLastFetchedDateTime(event, lastUpdatedTime);
    }
}
