package com.tml.uep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.exception.EmailParsingException;
import com.tml.uep.model.EmailMessage;
import com.tml.uep.model.Event;
import com.tml.uep.model.email.Attachment;
import com.tml.uep.model.email.DocumentEventMetadata;
import com.tml.uep.model.email.ProcessedEmail;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.model.s3.S3FileContent;
import com.tml.uep.model.s3.S3FileContentRequest;
import com.tml.uep.model.s3.S3FileCreationRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.activation.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailProcessorService {

    private String outboundDocumentBucketName;

    private EmailParserService emailParserService;

    private S3FileService s3FileService;

    @Autowired
    public EmailProcessorService(
            EmailParserService emailParserService,
            S3FileService s3FileService,
            @Value("${aws.s3.outbound-document-bucket-name}") String outboundDocumentBucketName) {
        this.emailParserService = emailParserService;
        this.s3FileService = s3FileService;
        this.outboundDocumentBucketName = outboundDocumentBucketName;
    }

    public ProcessedEmail processS3Email(S3FileContentRequest emailFileRequest) throws Exception {

        S3FileContent emailContent = s3FileService.getFileData(emailFileRequest);

        EmailMessage emailMessage = emailParserService.parse(emailContent.getContent());

        if (emailMessage == null) {
            throw new EmailParsingException("Could not parse email from the input");
        }

        DocumentEventMetadata eventMetaData;
        try {
            eventMetaData = extractEventMetaData(emailMessage);
        } catch (JsonProcessingException ex) {
            throw new EmailParsingException(
                    "Error occurred while converting to DocumentEventMetadata", ex);
        }

        DataSource dataSource = emailMessage.getAttachments().get(0);
        byte[] attachmentData =
                emailMessage.getAttachments().get(0).getInputStream().readAllBytes();
        Attachment attachment = new Attachment(dataSource.getName(), attachmentData);
        String md5AttachmentHash = DigestUtils.md5Hex(attachmentData);
        log.info("Mex Digest {}", md5AttachmentHash);

        List<String> s3Urls =
                uploadAttachmentToS3Bucket(attachment, eventMetaData.getDocumentType());
        return new ProcessedEmail(
                md5AttachmentHash,
                createOutboundEvent(eventMetaData, s3Urls.get(0)),
                eventMetaData.getTransactionNumber());
    }

    private OutboundEvent createOutboundEvent(
            DocumentEventMetadata eventMetaData, String s3FileUrl) {
        var data = new HashMap<String, Object>();
        data.put("dealershipName", eventMetaData.getDealershipName());
        data.put("vehicleRegNum", eventMetaData.getVehicleRegNum());
        data.put("fileUrl", s3FileUrl);

        return new OutboundEvent(
                eventMetaData.getDocumentType(),
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                eventMetaData.getBusinessUnit(),
                eventMetaData.getCustomerMobileNumber(),
                s3FileUrl,
                data);
    }

    private List<String> uploadAttachmentToS3Bucket(Attachment attachment, Event event) {
        ArrayList<String> s3Urls = new ArrayList<>();
        S3FileCreationRequest s3FileCreationRequest;

        s3FileCreationRequest =
                new S3FileCreationRequest(
                        OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond() + attachment.getName(),
                        outboundDocumentBucketName,
                        attachment.getData(),
                        "application/pdf",
                        event.name(),
                        true);
        String url = s3FileService.uploadFile(s3FileCreationRequest, new HashMap<>());
        s3Urls.add(url);

        return s3Urls;
    }

    private DocumentEventMetadata extractEventMetaData(EmailMessage emailMessage)
            throws JsonProcessingException {
        String emailBody = emailMessage.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        DocumentEventMetadata eventMetaData =
                objectMapper.readValue(emailBody, new TypeReference<>() {});
        return defaultMissingFields(eventMetaData);
    }

    private DocumentEventMetadata defaultMissingFields(DocumentEventMetadata eventMetaData) {
        String transactionNumber = eventMetaData.getTransactionNumber() + " : ";
        if (StringUtils.isBlank(eventMetaData.getCustomerMobileNumber())) {
            throw new EmailParsingException("Mobile number is missing in email body");
        }
        String defaultValue = "-";
        StringBuilder msgToBeLogged = new StringBuilder(transactionNumber);
        String vehicleNumber = eventMetaData.getVehicleRegNum();
        if (StringUtils.isBlank(vehicleNumber)) {
            msgToBeLogged.append(
                    "Vehicle Registration number is missing in email body, defaulting to - . ");
            vehicleNumber = defaultValue;
        }
        String dealershipName = eventMetaData.getDealershipName();
        if (StringUtils.isBlank(dealershipName)) {
            msgToBeLogged.append("Dealership name is missing in email body, defaulting to -");
            dealershipName = defaultValue;
        }
        String chassisNumber = eventMetaData.getChassisNumber();
        if (StringUtils.isBlank(chassisNumber)) {
            msgToBeLogged.append("Chassis Number name is missing in email body, defaulting to -");
            chassisNumber = defaultValue;
        }
        if (StringUtils.equalsAny(defaultValue, vehicleNumber, dealershipName, chassisNumber)) {
            log.info(msgToBeLogged.toString());
            return eventMetaData.createWith(dealershipName, vehicleNumber, chassisNumber);
        }
        return eventMetaData;
    }
}
