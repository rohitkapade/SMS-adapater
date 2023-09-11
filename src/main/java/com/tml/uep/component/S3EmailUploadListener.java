package com.tml.uep.component;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.tml.uep.exception.EmailParsingException;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.kafka.producers.EventMessageProducer;
import com.tml.uep.model.email.ProcessedEmail;
import com.tml.uep.model.entity.EmailSqsEvent;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.model.s3.S3FileContentRequest;
import com.tml.uep.service.EmailProcessorService;
import com.tml.uep.service.EmailSqsEventService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class S3EmailUploadListener {

    private String emailBucketName;

    private EmailProcessorService emailProcessorService;

    private EmailSqsEventService emailSqsEventService;

    private EventMessageProducer eventMessageProducer;

    @Value("${aws.email-sqs-queue}")
    private String queueName;

    @Autowired
    S3EmailUploadListener(
            EmailProcessorService emailProcessorService,
            EmailSqsEventService emailSqsEventService,
            EventMessageProducer eventMessageProducer,
            @Value("${aws.s3.email-bucket}") String emailBucketName) {
        this.emailProcessorService = emailProcessorService;
        this.emailSqsEventService = emailSqsEventService;
        this.eventMessageProducer = eventMessageProducer;
        this.emailBucketName = emailBucketName;
    }

    @SqsListener(
            value = "${aws.email-sqs-queue}",
            deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onS3UploadEvent(S3EventNotification event) {
        log.info("Event received on sqs queue {} : {}", queueName, event.toJson());
        List<S3EventNotification.S3EventNotificationRecord> s3EventNotificationRecords =
                event.getRecords();
        if (s3EventNotificationRecords != null && !s3EventNotificationRecords.isEmpty()) {
            s3EventNotificationRecords.forEach(this::getS3EventNotificationRecordConsumer);
        }
    }

    private void getS3EventNotificationRecordConsumer(
            S3EventNotification.S3EventNotificationRecord eventRecord) {

        S3EventNotification.S3ObjectEntity entity = eventRecord.getS3().getObject();
        String fileName = entity.getKey();
        // SQS can potentially send same event twice in rare cases
        if (isDuplicateSQSEvent(entity)) {
            log.info("Preventing duplicate SQS message from being processed for file {}", fileName);
            return;
        }

        S3FileContentRequest request = new S3FileContentRequest(emailBucketName, fileName);
        try {
            ProcessedEmail processedEmail = emailProcessorService.processS3Email(request);

            String md5Hash = processedEmail.getMd5AttachmentHash();
            if (isDuplicatePdfAttachment(md5Hash)) {
                log.info(
                        "Preventing duplicate CRM email message from being processed for file {} with Hash {}",
                        fileName,
                        md5Hash);
                return;
            }

            OutboundEvent outboundEvent = processedEmail.getOutboundEvent();
            eventMessageProducer.send(outboundEvent.getEventId(), outboundEvent);

            // save email event to facilitate duplicate check later
            emailSqsEventService.saveEmailEvent(
                    new EmailSqsEvent(
                            fileName,
                            entity.getSequencer(),
                            eventRecord.getEventTime().toString(),
                            md5Hash,
                            processedEmail.getCrmTransactionId()));
        } catch (EmailParsingException ex) {
            String errorMsg = "Error parsing email file";
            log.error(errorMsg, ex);
        } catch (IOException e) {
            String errorMsg = "Error obtaining input stream from attachment";
            log.error(errorMsg, e);
            throw new ExternalSystemException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error processing email file";
            log.error(errorMsg, e);
            throw new ExternalSystemException(errorMsg, e);
        }
    }

    private boolean isDuplicateSQSEvent(S3EventNotification.S3ObjectEntity entity) {
        Optional<String> optionalSequencer =
                emailSqsEventService.getSequencerByFileName(entity.getKey());
        return optionalSequencer.isPresent()
                && optionalSequencer.get().equals(entity.getSequencer());
    }

    private boolean isDuplicatePdfAttachment(String md5AttachmentHash) {
        return emailSqsEventService.isMatchingMd5AttachmentHashFound(md5AttachmentHash);
    }
}
