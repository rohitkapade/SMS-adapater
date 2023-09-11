package com.tml.uep.service;

import com.tml.uep.model.Event;
import com.tml.uep.model.entity.EventDataRetrievalHistory;
import com.tml.uep.repository.EventDataRetrievalHistoryRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventDataRetrievalHistoryService {

    @Autowired private EventDataRetrievalHistoryRepository historyRepository;

    public Optional<OffsetDateTime> getLastFetchedDateTime(Event event) {
        OffsetDateTime fromDateTime;
        Optional<EventDataRetrievalHistory> historyOptional =
                historyRepository.findById(event.name());

        if (historyOptional.isEmpty()) {
            return Optional.empty();
        }

        fromDateTime = historyOptional.get().getDateTime();
        OffsetDateTime fromDateTimeInUTC =
                OffsetDateTime.ofInstant(fromDateTime.toInstant(), ZoneOffset.UTC);
        return Optional.of(fromDateTimeInUTC);
    }

    public EventDataRetrievalHistory saveLastFetchedDateTime(
            Event event, OffsetDateTime lastUpdatedTime) {
        EventDataRetrievalHistory eventDataRetrievalHistory =
                new EventDataRetrievalHistory(event.name(), lastUpdatedTime);
        return historyRepository.save(eventDataRetrievalHistory);
    }
}
