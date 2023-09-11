package com.tml.uep.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.Event;
import com.tml.uep.model.entity.EventDataRetrievalHistory;
import com.tml.uep.repository.EventDataRetrievalHistoryRepository;
import com.tml.uep.utils.DateUtils;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Import(AWSTestConfiguration.class)
public class SalesKafkaProducerEventServiceTest {

    @MockBean private KafkaProducerEventService kafkaEventProducer;

    @Autowired private SalesEventService salesEventService;

    @Autowired private EventDataRetrievalHistoryRepository historyRepository;

    @Test
    public void shouldPassDefaultDateRangeWhenItIsMissing()
            throws ExecutionException, InterruptedException {

        salesEventService.getAndPublishDPlus15ServicingBenefitsEvents(null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        ArgumentCaptor<OffsetDateTime> offsetDateTimeArgumentCaptor =
                ArgumentCaptor.forClass(OffsetDateTime.class);

        verify(kafkaEventProducer, times(1))
                .sendAllEventMessagesToKafka(
                        eventArgumentCaptor.capture(),
                        offsetDateTimeArgumentCaptor.capture(),
                        offsetDateTimeArgumentCaptor.capture(),
                        any());
        Assert.assertEquals(Event.SERVICING_BENEFITS, eventArgumentCaptor.getValue());
        OffsetDateTime fromDateDay =
                DateUtils.getStartDateTimeOf(OffsetDateTime.now().minusDays(15));
        Assert.assertEquals(fromDateDay, offsetDateTimeArgumentCaptor.getAllValues().get(0));
        Assert.assertEquals(
                DateUtils.getEndDateTimeOf(fromDateDay),
                offsetDateTimeArgumentCaptor.getAllValues().get(1));
    }

    @Test
    public void shouldPassDateRangeBasedOnHistoryWhenAvailable()
            throws ExecutionException, InterruptedException {
        OffsetDateTime lastTimeFetched =
                DateUtils.getStartDateTimeOf(OffsetDateTime.now(ZoneOffset.UTC).minusDays(17));
        historyRepository.save(
                new EventDataRetrievalHistory(Event.SERVICING_BENEFITS.name(), lastTimeFetched));
        salesEventService.getAndPublishDPlus15ServicingBenefitsEvents(null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        ArgumentCaptor<OffsetDateTime> offsetDateTimeArgumentCaptor =
                ArgumentCaptor.forClass(OffsetDateTime.class);

        verify(kafkaEventProducer, times(1))
                .sendAllEventMessagesToKafka(
                        eventArgumentCaptor.capture(),
                        offsetDateTimeArgumentCaptor.capture(),
                        offsetDateTimeArgumentCaptor.capture(),
                        any());
        Assert.assertEquals(Event.SERVICING_BENEFITS, eventArgumentCaptor.getValue());
        OffsetDateTime expectedRangeEnd =
                DateUtils.getEndDateTimeOf(OffsetDateTime.now(ZoneOffset.UTC).minusDays(15));
        Assert.assertEquals(lastTimeFetched, offsetDateTimeArgumentCaptor.getAllValues().get(0));
        Assert.assertEquals(expectedRangeEnd, offsetDateTimeArgumentCaptor.getAllValues().get(1));
    }

    @Test
    public void shouldForwardExactDateRangeWhenItIsPassed()
            throws ExecutionException, InterruptedException {

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime tomorrow = now.plusDays(1);
        salesEventService.getAndPublishDPlus15ServicingBenefitsEvents(now, now.plusDays(1));

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        ArgumentCaptor<OffsetDateTime> offsetDateTimeArgumentCaptor =
                ArgumentCaptor.forClass(OffsetDateTime.class);

        verify(kafkaEventProducer, times(1))
                .sendAllEventMessagesToKafka(
                        eventArgumentCaptor.capture(),
                        offsetDateTimeArgumentCaptor.capture(),
                        offsetDateTimeArgumentCaptor.capture(),
                        any());
        Assert.assertEquals(Event.SERVICING_BENEFITS, eventArgumentCaptor.getValue());

        Assert.assertEquals(
                now.getDayOfMonth(),
                offsetDateTimeArgumentCaptor.getAllValues().get(0).getDayOfMonth());
        Assert.assertEquals(
                tomorrow.getDayOfMonth(),
                offsetDateTimeArgumentCaptor.getAllValues().get(1).getDayOfMonth());
    }
}
