package com.tml.uep.controller;

import com.tml.uep.service.SalesEventService;
import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sales/events")
@Slf4j
public class SalesEventController {

    @Autowired private SalesEventService salesEventService;

    @PostMapping("dPlus15ServicingBenefits")
    public ResponseEntity<Void> processDPlus15ServicingBenefits(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        salesEventService.getAndPublishDPlus15ServicingBenefitsEvents(fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping("customerAppLinks")
    public ResponseEntity<Void> processDPlus1CustomerAppLinks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        salesEventService.getAndPublishCustomerAppLinksEvents(fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/welcomeLetter")
    public ResponseEntity<Void> processwelcomeLetter(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        salesEventService.getAndPublishWelcomeLetterEvents(fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping("dMinus9WorkshopTourVideo")
    public ResponseEntity<Void> processWorkshopTourVideo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    OffsetDateTime toDateTime)
            throws ExecutionException, InterruptedException {

        salesEventService.getAndPublishDMinus9WorkshopTourVideo(fromDateTime, toDateTime);
        return ResponseEntity.ok().build();
    }
}
