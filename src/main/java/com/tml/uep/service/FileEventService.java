package com.tml.uep.service;

import com.tml.uep.kafka.producers.EventMessageProducer;
import com.tml.uep.model.*;
import com.tml.uep.model.entity.FileEvent;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.model.s3.S3FileCreationRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileEventService {

    @Autowired private EventMessageProducer eventMessageProducer;

    @Value("${domain.business-unit}")
    private BusinessUnit businessUnit;

    @Value("${aws.s3.outbound-document-bucket-name}")
    private String uploadedFilesS3BaseURL;

    @Value("${aws.s3.outbound-document-bucket-name}")
    private String outboundDocumentBucketName;

    @Autowired private PdfTemplatingService pdfTemplatingService;

    @Autowired private EventExpiryTimeConfig eventExpiryTimeConfig;

    @Autowired private S3FileService s3FileService;

    private void createOutBoundEventsAndSendToKafka(List<FileEvent> fileEvents) {
        fileEvents.stream()
                .map(fileEvent -> fileEvent.createOutBoundEvent(businessUnit))
                .forEach(
                        outboundEvent -> {
                            log.info("File event produced to kafka : {}", outboundEvent.toString());
                            eventMessageProducer.send(outboundEvent.getEventId(), outboundEvent);
                        });
    }

    public void generatePDFFromTemplateAndSendToKafka(
            String templateName,
            Map<String, String> placeholderMap,
            OutboundEventConverter outboundEventType,
            String mobileNumber)
            throws Exception {

        String welcomeLetterFileName = mobileNumber + "Welcome Letter.pdf";
        File outputPdf =
                pdfTemplatingService.getPDFFromTemplate(
                        templateName, placeholderMap, welcomeLetterFileName);
        InputStream pdfInputStream = getFileInputStreamFromOutputStream(outputPdf);
        S3FileCreationRequest s3FileCreationRequest =
                new S3FileCreationRequest(
                        welcomeLetterFileName,
                        outboundDocumentBucketName,
                        pdfInputStream.readAllBytes(),
                        "application/pdf",
                        Event.WELCOME_LETTER.name(),
                        true);
        String s3FileUrl = s3FileService.uploadFile(s3FileCreationRequest, new HashMap<>());
        OutboundEvent outboundEvent = outboundEventType.convertToOutboundEvent(businessUnit);
        FileEvent fileEvent =
                new FileEvent(
                        outboundEvent.getEventId(),
                        outboundEvent.getReceiverId(),
                        outboundEvent.getEventDateTime(),
                        Event.WELCOME_LETTER,
                        EventStatus.RECEIVED,
                        s3FileUrl,
                        outboundEvent.getPayload());
        createOutBoundEventsAndSendToKafka(List.of(fileEvent));
    }

    private InputStream getFileInputStreamFromOutputStream(File fileOutput) throws IOException {
        return new FileInputStream(fileOutput);
    }
}
