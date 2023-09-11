package com.tml.uep.service;

import com.tml.uep.model.*;
import com.tml.uep.solr_api.dto.SolrApiResponse;
import com.tml.uep.utils.DateUtils;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ServicingEventService {

    @Autowired private EventProcessingService eventProcessingService;

    @Value("${domain.business-unit}")
    private BusinessUnit businessUnit;

    public void getAndPublishDMinus1ServicingScheduledEvents(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
        DateRange dateRangeIfNull =
                new DateRange(
                        DateUtils.getStartDateTimeOf(startTime),
                        DateUtils.getEndDateTimeOf(startTime));

        if (BusinessUnit.PV.equals(businessUnit)) {
            ParameterizedTypeReference<SolrApiResponse<ServiceScheduledEvent>> responseType =
                    new ParameterizedTypeReference<>() {};
            eventProcessingService.getAndProcessEventWithoutHistory(
                    responseType,
                    Event.SERVICE_APPOINTMENT,
                    fromDateTime,
                    toDateTime,
                    dateRangeIfNull);
        } else {
            ParameterizedTypeReference<SolrApiResponse<CVServiceScheduledEvent>> responseType =
                    new ParameterizedTypeReference<>() {};
            eventProcessingService.getAndProcessEventWithoutHistory(
                    responseType,
                    Event.SERVICE_APPOINTMENT,
                    fromDateTime,
                    toDateTime,
                    dateRangeIfNull);
        }
    }

    public void getAndPublishServicingScheduledPickupDropEvents(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        OffsetDateTime startTime = DateUtils.getTodaysStartTime();
        DateRange dateRangeIfNull =
                new DateRange(
                        DateUtils.getTodaysStartTime(), DateUtils.getEndDateTimeOf(startTime));

        ParameterizedTypeReference<SolrApiResponse<ServiceScheduledPickupDropEvent>> responseType =
                new ParameterizedTypeReference<>() {};
        eventProcessingService.getAndProcessEventWithoutHistory(
                responseType,
                Event.SERVICE_APPOINTMENT_PICKUP_DROP,
                fromDateTime,
                toDateTime,
                dateRangeIfNull);
    }

    public void getAndPublishReadyForDeliveryVehicles(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        ParameterizedTypeReference<SolrApiResponse<ReadyForDeliveryEvent>> responseType =
                new ParameterizedTypeReference<>() {};
        DateRange dateRangeIfNull =
                new DateRange(DateUtils.getTodaysStartTime(), OffsetDateTime.now(ZoneOffset.UTC));
        eventProcessingService.getAndProcessEventWithHistory(
                responseType, Event.READY_FOR_DELIVERY, fromDateTime, toDateTime, dateRangeIfNull);
    }

    public void getAndPublishNextServiceReminderVehicles(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        if (BusinessUnit.PV.equals(businessUnit)) {
            ParameterizedTypeReference<SolrApiResponse<NextServiceReminderEvent>> responseType =
                    new ParameterizedTypeReference<>() {};
            eventProcessingService.logInfoAndProduceToKafka(
                    responseType, Event.NEXT_SERVICE_REMINDER, fromDateTime, toDateTime);
        } else {
            ParameterizedTypeReference<SolrApiResponse<CVNextServiceReminderEvent>> responseType =
                    new ParameterizedTypeReference<>() {};
            eventProcessingService.logInfoAndProduceToKafka(
                    responseType, Event.NEXT_SERVICE_REMINDER, fromDateTime, toDateTime);
        }
    }

    public void getAndPublishDPlus3ServiceRequestClosed(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        ParameterizedTypeReference<SolrApiResponse<ServiceQualityFeedbackEvent>> responseType =
                new ParameterizedTypeReference<>() {};
        OffsetDateTime dateOfServiceToBeUsed = OffsetDateTime.now(ZoneOffset.UTC).minusDays(3);
        DateRange dateRangeIfNull =
                new DateRange(
                        DateUtils.getStartDateTimeOf(dateOfServiceToBeUsed),
                        DateUtils.getEndDateTimeOf(dateOfServiceToBeUsed));
        eventProcessingService.getAndProcessEventWithHistory(
                responseType,
                Event.SERVICE_QUALITY_FEEDBACK_LINK,
                fromDateTime,
                toDateTime,
                dateRangeIfNull);
    }

    public void getAndPublishServiceInstantFeedbackLinks(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        ParameterizedTypeReference<SolrApiResponse<ServicingInstantFeedbackEvent>> responseType =
                new ParameterizedTypeReference<>() {};
        eventProcessingService.getAndProcessShortTermEvent(
                responseType, Event.SERVICE_INSTANT_FEEDBACK_LINK, fromDateTime, toDateTime);
    }

    public void getAndPublishServiceAppointmentBeforeDealershipConfirmation(
            OffsetDateTime fromDateTime, OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        ParameterizedTypeReference<SolrApiResponse<ServiceAppointmentBeforeDealershipConfirmation>>
                responseType = new ParameterizedTypeReference<>() {};
        eventProcessingService.getAndProcessShortTermEvent(
                responseType,
                Event.SERVICE_APPOINTMENT_BEFORE_DEALERSHIP_CONFIRMATION,
                fromDateTime,
                toDateTime);
    }
}
