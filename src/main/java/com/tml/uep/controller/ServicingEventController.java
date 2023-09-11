package com.tml.uep.controller;

import com.tml.uep.service.ServicingEventService;
import com.tml.uep.utils.DateUtils;
import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("servicing/events")
@Slf4j
public class ServicingEventController {

    @Autowired private ServicingEventService servicingEventService;

    @PostMapping("dMinus1ServicingScheduled")
    public ResponseEntity<Void> processDMinus1ServicingScheduledEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        servicingEventService.getAndPublishDMinus1ServicingScheduledEvents(
                fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping("pickupDropServicingScheduled")
    public ResponseEntity<Void> processPickupDropServicingScheduledEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        servicingEventService.getAndPublishServicingScheduledPickupDropEvents(
                fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping("readyForDelivery")
    public ResponseEntity<Void> processReadyForDeliveryVehicles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        servicingEventService.getAndPublishReadyForDeliveryVehicles(fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping("nextServiceReminder/{days}")
    public ResponseEntity<Void> processNextServiceReminderVehicles(
            @PathVariable int days,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        if (fromDateTime == null || toDateTime == null) {
            OffsetDateTime now = DateUtils.getTodaysStartTime();
            fromDateTime = now.plusDays(days);
            toDateTime = DateUtils.getEndDateTimeOf(fromDateTime);
        }
        servicingEventService.getAndPublishNextServiceReminderVehicles(fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping("serviceInstantFeedback")
    public ResponseEntity<Void> processServiceInstantFeedbackLinks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        servicingEventService.getAndPublishServiceInstantFeedbackLinks(fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping("dPlus3ServiceRequestClosed")
    public ResponseEntity<Void> processDPlus3ServiceRequestClosed(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        servicingEventService.getAndPublishDPlus3ServiceRequestClosed(fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping("serviceAppointmentBeforeDealershipConfirmation")
    public ResponseEntity<Void> processServiceAppointmentBeforeDealershipConfirmation(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {
        servicingEventService.getAndPublishServiceAppointmentBeforeDealershipConfirmation(
                fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }
}
