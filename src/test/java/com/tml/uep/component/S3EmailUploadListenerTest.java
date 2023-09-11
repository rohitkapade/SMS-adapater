package com.tml.uep.component;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.kafka.producers.EventMessageProducer;
import com.tml.uep.model.Event;
import com.tml.uep.model.entity.EmailSqsEvent;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.model.s3.S3FileContentRequest;
import com.tml.uep.repository.EmailSqsEventRepository;
import com.tml.uep.service.EmailParserService;
import com.tml.uep.service.EmailProcessorService;
import com.tml.uep.service.EmailSqsEventService;
import com.tml.uep.service.S3FileService;
import com.tml.uep.testUtils.TestMailUtils;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Import({AWSTestConfiguration.class})
public class S3EmailUploadListenerTest {

    @Autowired private EmailParserService emailParserService;

    @MockBean private EventMessageProducer eventMessageProducer;

    @Autowired private EmailSqsEventRepository emailSqsEventRepository;

    private AmazonS3 s3Client;

    private S3FileService s3FileService;

    private EmailProcessorService emailProcessorService;

    private S3EmailUploadListener uploadListener;

    ClassLoader classLoader = getClass().getClassLoader();

    @Value("${aws.s3.email-bucket}")
    private String emailBucketName;

    @Value("${aws.s3.outbound-document-bucket-name}")
    private String outboundDocumentBucketName;

    @Value("${aws.s3.signed-url-expiry-mins}")
    private String signedUrlExpiryMinutes;

    private String expectedS3PdfUrl =
            "https://engagex-mail-extracted-pdfs.s3.amazonaws.com/SERVICE_INVOICE/CVBUServiceInvoice.pdf";
    private String s3PdfFileNameWithKey = Event.SERVICE_INVOICE.name() + "/CVBUServiceInvoice.pdf";

    @Value("${domain.crm-sender-email}")
    private String senderEmail;

    @Before
    public void setUpMockS3Client() {
        s3Client = Mockito.mock(AmazonS3.class);
        s3FileService = new S3FileService(s3Client, signedUrlExpiryMinutes);

        emailSqsEventRepository.deleteAll();

        emailProcessorService =
                Mockito.spy(
                        new EmailProcessorService(
                                emailParserService, s3FileService, outboundDocumentBucketName));

        uploadListener =
                new S3EmailUploadListener(
                        emailProcessorService,
                        new EmailSqsEventService(emailSqsEventRepository),
                        eventMessageProducer,
                        emailBucketName);
    }

    @Test
    public void shouldProcessEmailUploadEvent() throws Exception {

        String subject = "SERVICE_INVOICE";
        String emailBody =
                new TestMailUtils()
                        .createStringifiedEmail(
                                subject,
                                "emails/serviceInvoice.json",
                                "pdfs/CVBUServiceInvoice.pdf",
                                senderEmail);
        mockS3GetObjectReturnValue(subject, emailBody);
        Mockito.when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL(expectedS3PdfUrl));

        URL url = classLoader.getResource("sqs-events/sample-sqs-event.json");
        String notificationText = Files.readString(Path.of(url.toURI()));
        ObjectMapper objectMapper = new ObjectMapper();
        S3EventNotification eventNotification =
                objectMapper.readValue(notificationText, S3EventNotification.class);

        uploadListener.onS3UploadEvent(eventNotification);

        Mockito.verify(emailProcessorService, times(1))
                .processS3Email(any(S3FileContentRequest.class));
        Mockito.verify(s3Client, Mockito.times(1)).getObject(any());
        Mockito.verify(s3Client, Mockito.times(1)).putObject(any());
        Mockito.verify(s3Client, Mockito.times(1))
                .generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
        Optional<EmailSqsEvent> sqsEventOptional = emailSqsEventRepository.findById(subject);

        Assert.assertTrue(sqsEventOptional.isPresent());
        Assert.assertEquals(
                eventNotification.getRecords().get(0).getS3().getObject().getSequencer(),
                sqsEventOptional.get().getSequencer());

        Mockito.verify(eventMessageProducer, Mockito.times(1))
                .send(anyString(), any(OutboundEvent.class));
    }

    @Test
    public void shouldThrowExceptionForEmailsOtherThanFromCRM() throws Exception {

        String subject = "SERVICE_INVOICE";
        String emailBody =
                new TestMailUtils()
                        .createStringifiedEmail(
                                subject,
                                "emails/serviceInvoice.json",
                                "pdfs/CVBUServiceInvoice.pdf",
                                "testmail@xyz.com");
        mockS3GetObjectReturnValue(subject, emailBody);

        URL url = classLoader.getResource("sqs-events/sample-sqs-event.json");
        String notificationText = Files.readString(Path.of(url.toURI()));
        ObjectMapper objectMapper = new ObjectMapper();
        S3EventNotification eventNotification =
                objectMapper.readValue(notificationText, S3EventNotification.class);

        uploadListener.onS3UploadEvent(eventNotification);

        Mockito.verify(emailProcessorService, times(1))
                .processS3Email(any(S3FileContentRequest.class));
        Mockito.verify(s3Client, Mockito.times(1)).getObject(any());
        Mockito.verify(s3Client, Mockito.times(0)).putObject(any());
        Mockito.verify(s3Client, Mockito.times(0))
                .generatePresignedUrl(any(GeneratePresignedUrlRequest.class));

        Mockito.verify(eventMessageProducer, Mockito.times(0))
                .send(anyString(), any(OutboundEvent.class));
    }

    @Test
    public void shouldNotProcessDuplicateSQSEmailUploadEvent() throws Exception {

        String subject = "SERVICE_INVOICE";
        String emailBody =
                new TestMailUtils()
                        .createStringifiedEmail(
                                subject,
                                "emails/serviceInvoice.json",
                                "pdfs/CVBUServiceInvoice.pdf",
                                senderEmail);
        mockS3GetObjectReturnValue(subject, emailBody);
        Mockito.when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL(expectedS3PdfUrl));

        URL url = classLoader.getResource("sqs-events/sample-sqs-event.json");
        String notificationText = Files.readString(Path.of(url.toURI()));
        ObjectMapper objectMapper = new ObjectMapper();
        S3EventNotification eventNotification =
                objectMapper.readValue(notificationText, S3EventNotification.class);

        emailSqsEventRepository.save(
                new EmailSqsEvent(
                        subject,
                        eventNotification.getRecords().get(0).getS3().getObject().getSequencer(),
                        eventNotification.getRecords().get(0).getEventTime().toString(),
                        "",
                        ""));

        uploadListener.onS3UploadEvent(eventNotification);

        Mockito.verify(emailProcessorService, times(0))
                .processS3Email(any(S3FileContentRequest.class));

        Mockito.verify(s3Client, Mockito.times(0)).getObject(any());
        Mockito.verify(s3Client, Mockito.times(0)).putObject(any());
        Mockito.verify(s3Client, Mockito.times(0))
                .generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
        Mockito.verify(eventMessageProducer, Mockito.times(0))
                .send(anyString(), any(OutboundEvent.class));
    }

    @Test
    public void shouldNotProcessDuplicateEmailWithSamePDF() throws Exception {

        String subject = "SERVICE_INVOICE";
        String emailBody =
                new TestMailUtils()
                        .createStringifiedEmail(
                                subject,
                                "emails/serviceInvoice.json",
                                "pdfs/CVBUServiceInvoice.pdf",
                                senderEmail);
        mockS3GetObjectReturnValue(subject, emailBody);
        Mockito.when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL(expectedS3PdfUrl));

        URL url = classLoader.getResource("sqs-events/sample-sqs-event.json");
        String notificationText = Files.readString(Path.of(url.toURI()));
        ObjectMapper objectMapper = new ObjectMapper();
        S3EventNotification eventNotification =
                objectMapper.readValue(notificationText, S3EventNotification.class);

        uploadListener.onS3UploadEvent(eventNotification);

        mockS3GetObjectReturnValue("SERVICE_INVOICE1", emailBody);
        url = classLoader.getResource("sqs-events/duplicate-pdf-event.json");
        notificationText = Files.readString(Path.of(url.toURI()));
        S3EventNotification eventNotification2 =
                objectMapper.readValue(notificationText, S3EventNotification.class);
        uploadListener.onS3UploadEvent(eventNotification2);

        Mockito.verify(emailProcessorService, times(2))
                .processS3Email(any(S3FileContentRequest.class));

        Mockito.verify(s3Client, Mockito.times(2)).getObject(any());
        Mockito.verify(s3Client, Mockito.times(2)).putObject(any());
        Mockito.verify(s3Client, Mockito.times(2))
                .generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
        Mockito.verify(eventMessageProducer, Mockito.times(1))
                .send(anyString(), any(OutboundEvent.class));
    }

    @Test(expected = ExternalSystemException.class)
    public void shouldThrowExceptionWhenS3FileDownloadFails() throws Exception {

        Mockito.when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(new SdkClientException("S3 encountered error"));

        URL url = classLoader.getResource("sqs-events/sample-sqs-event.json");
        String notificationText = Files.readString(Path.of(url.toURI()));
        ObjectMapper objectMapper = new ObjectMapper();
        S3EventNotification eventNotification =
                objectMapper.readValue(notificationText, S3EventNotification.class);

        uploadListener.onS3UploadEvent(eventNotification);
        Mockito.verify(emailProcessorService, times(1))
                .processS3Email(any(S3FileContentRequest.class));
        Mockito.verify(s3Client, Mockito.times(1)).getObject(any());
        Mockito.verify(s3Client, Mockito.times(0)).putObject(any());
        Mockito.verify(s3Client, Mockito.times(0)).getUrl(any(String.class), any(String.class));
    }

    private void mockS3GetObjectReturnValue(String s3fileNameWithKey, String emailBody)
            throws UnsupportedEncodingException {
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new StringInputStream(emailBody));
        s3Object.setObjectMetadata(new ObjectMetadata());

        Mockito.when(s3Client.getObject(new GetObjectRequest(emailBucketName, s3fileNameWithKey)))
                .thenReturn(s3Object);
    }
}
