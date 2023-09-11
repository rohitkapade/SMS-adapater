package com.tml.uep.service;

import com.tml.uep.kafka.producers.ErrorEventsProducer;
import com.tml.uep.kafka.producers.EventMessageProducer;
import com.tml.uep.model.BusinessUnit;
import com.tml.uep.model.Event;
import com.tml.uep.model.OutboundEventConverter;
import com.tml.uep.model.entity.EventDataRetrievalHistory;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.repository.EventDataRetrievalHistoryRepository;
import com.tml.uep.solr_api.EventsFetcherSolrService;
import com.tml.uep.solr_api.dto.SolrApiResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerEventService {

    @Autowired private EventsFetcherSolrService eventsFetcherSolrService;

    @Value("${domain.business-unit}")
    private BusinessUnit businessUnit;

    @Autowired private EventMessageProducer eventMessageProducer;

    @Autowired private ErrorEventsProducer errorEventsProducer;

    @Autowired private EventDataRetrievalHistoryRepository historyRepository;

    @Value("#{'${domain.file-events}'.split(',')}")
    private List<String> fileEvents;

    public <T extends OutboundEventConverter> void sendAllEventMessagesToKafka(
            Event useCase,
            OffsetDateTime start,
            OffsetDateTime end,
            ParameterizedTypeReference<SolrApiResponse<T>> responseType)
            throws ExecutionException, InterruptedException {

        OffsetDateTime rangeStartTimeInUTC =
                OffsetDateTime.ofInstant(start.toInstant(), ZoneOffset.UTC);
        OffsetDateTime rangeEndTimeInUTC =
                OffsetDateTime.ofInstant(end.toInstant(), ZoneOffset.UTC);
        final List<CompletableFuture<SolrApiResponse<T>>> responses =
                eventsFetcherSolrService.<T>getRecordsForEvent(
                        useCase, rangeStartTimeInUTC, rangeEndTimeInUTC, responseType);

        processNonFileEvents(responses);

        historyRepository.save(new EventDataRetrievalHistory(useCase.name(), rangeEndTimeInUTC));
    }

    private <T extends OutboundEventConverter> void processNonFileEvents(
            List<CompletableFuture<SolrApiResponse<T>>> responses) {
        responses.forEach(
                response -> {
                    response.thenAccept(
                            records -> {
                                final List<T> data = records.getData();
                                data.forEach(
                                        event -> {
                                            OutboundEvent outboundEvent =
                                                    event.convertToOutboundEvent(businessUnit);
                                            if (!event.isValid()) {
                                                sendErrorEvent(event, outboundEvent);
                                                return;
                                            }

                                            eventMessageProducer.send(
                                                    outboundEvent.getEventId(), outboundEvent);
                                        });
                            });
                });
    }

    private <T extends OutboundEventConverter> void sendErrorEvent(
            T event, OutboundEvent outboundEvent) {
        log.warn(
                "Event filtered because of missing mandatory fields, Event: {}, Id : {}",
                event.getClass().getName(),
                event.getId());
        errorEventsProducer.send(outboundEvent.getEventId(), outboundEvent);
    }
}
