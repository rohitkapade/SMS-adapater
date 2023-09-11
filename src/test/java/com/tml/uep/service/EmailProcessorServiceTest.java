package com.tml.uep.service;

import static org.mockito.ArgumentMatchers.any;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.StringInputStream;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.exception.EmailParsingException;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.Event;
import com.tml.uep.model.email.ProcessedEmail;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.model.s3.S3FileContentRequest;
import com.tml.uep.testUtils.TestMailUtils;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Import({AWSTestConfiguration.class})
public class EmailProcessorServiceTest {

    private AmazonS3 s3Client;

    private ClassLoader classLoader = getClass().getClassLoader();

    @Value("${aws.s3.email-bucket}")
    private String emailBucketName;

    @Value("${aws.s3.outbound-document-bucket-name}")
    private String outboundDocumentBucketName;

    @Value("${aws.s3.signed-url-expiry-mins}")
    private String signedUrlExpiryMinutes;

    private EmailProcessorService emailProcessorService;

    private String expectedS3PdfUrl =
            "https://engagex-mail-extracted-pdfs.s3.amazonaws.com/SERVICE_INVOICE/CVBUServiceInvoice.pdf";

    @Value("${domain.crm-sender-email}")
    private String senderEmail;

    @Autowired private EmailParserService emailParserService;

    @Before
    public void setUpMockS3Client() {
        s3Client = Mockito.mock(AmazonS3.class);
        emailProcessorService =
                new EmailProcessorService(
                        emailParserService,
                        new S3FileService(s3Client, signedUrlExpiryMinutes),
                        outboundDocumentBucketName);
    }

    @Test
    public void shouldProcessEmailAndUploadAttachment() throws Exception {

        String subject = "SERVICE_INVOICE";
        String emailBody =
                new TestMailUtils()
                        .createStringifiedEmail(
                                subject,
                                "emails/serviceInvoice.json",
                                "pdfs/CVBUServiceInvoice.pdf",
                                senderEmail);
        mockS3GetObjectReturnValue(subject, emailBody);

        S3FileContentRequest request = new S3FileContentRequest(emailBucketName, subject);

        Mockito.when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL(expectedS3PdfUrl));

        ProcessedEmail processedEmail = emailProcessorService.processS3Email(request);
        Mockito.verify(s3Client, Mockito.times(1)).getObject(any());
        Mockito.verify(s3Client, Mockito.times(1)).putObject(any());
        Mockito.verify(s3Client, Mockito.times(1))
                .generatePresignedUrl(any(GeneratePresignedUrlRequest.class));

        OutboundEvent event = processedEmail.getOutboundEvent();
        Map<String, Object> map = event.getPayload();
        Assert.assertEquals("SANYA MOTORS - JALNA ROAD", map.get("dealershipName"));
        Assert.assertEquals("MH02ABCD12", map.get("vehicleRegNum"));
        Assert.assertEquals("919004244790", event.getReceiverId());
        Assert.assertNotNull(event.getFileUrl());
        Assert.assertNotNull(processedEmail.getMd5AttachmentHash());
    }

    @Test(expected = EmailParsingException.class)
    public void shouldErrorOutForMissingPhoneNumber() throws Exception {

        String subject = "SERVICE_INVOICE";
        String emailBody =
                new TestMailUtils()
                        .createStringifiedEmail(
                                subject,
                                "emails/missingPhoneNumber.json",
                                "pdfs/CVBUServiceInvoice.pdf",
                                senderEmail);
        mockS3GetObjectReturnValue(subject, emailBody);

        S3FileContentRequest request = new S3FileContentRequest(emailBucketName, subject);

        emailProcessorService.processS3Email(request);

        Mockito.verify(s3Client, Mockito.times(1)).getObject(any());
        Mockito.verify(s3Client, Mockito.times(0)).putObject(any());
        Mockito.verify(s3Client, Mockito.times(0))
                .generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    public void shouldReplaceMissingVehicleNumberAndDealershipNameWithHyphen() throws Exception {

        String subject = "SERVICE_INVOICE";
        String emailBody =
                new TestMailUtils()
                        .createStringifiedEmail(
                                subject,
                                "emails/missingVehicleNumberDealershipName.json",
                                "pdfs/CVBUServiceInvoice.pdf",
                                senderEmail);
        mockS3GetObjectReturnValue(subject, emailBody);

        S3FileContentRequest request = new S3FileContentRequest(emailBucketName, subject);

        Mockito.when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL(expectedS3PdfUrl));

        ProcessedEmail processedEmail = emailProcessorService.processS3Email(request);
        Mockito.verify(s3Client, Mockito.times(1)).getObject(any());
        Mockito.verify(s3Client, Mockito.times(1)).putObject(any());
        Mockito.verify(s3Client, Mockito.times(1))
                .generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
        String hyphen = "-";
        OutboundEvent event = processedEmail.getOutboundEvent();
        Map<String, Object> map = event.getPayload();
        Assert.assertEquals(hyphen, map.get("dealershipName"));
        Assert.assertEquals(hyphen, map.get("vehicleRegNum"));
        Assert.assertNotNull(event.getReceiverId());
        Assert.assertNotNull(event.getFileUrl());
        Assert.assertNotNull(processedEmail.getMd5AttachmentHash());
    }

    private void mockS3GetObjectReturnValue(String s3fileNameWithKey, String emailBody)
            throws UnsupportedEncodingException {
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new StringInputStream(emailBody));
        s3Object.setObjectMetadata(new ObjectMetadata());

        Mockito.when(s3Client.getObject(new GetObjectRequest(emailBucketName, s3fileNameWithKey)))
                .thenReturn(s3Object);
    }

    @Test(expected = ExternalSystemException.class)
    public void shouldThrowExceptionWhenEmailDownloadFails() throws Exception {
        String subject = "SERVICE_INVOICE";
        String s3fileNameWithKey = Event.SERVICE_INVOICE.name() + "/" + subject;

        Mockito.when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(new SdkClientException("S3 encountered error"));

        S3FileContentRequest request = new S3FileContentRequest(emailBucketName, s3fileNameWithKey);

        emailProcessorService.processS3Email(request);
        Mockito.verify(s3Client, Mockito.times(1)).getObject(any());
        Mockito.verify(s3Client, Mockito.times(0)).putObject(any());
    }

    @Test(expected = ExternalSystemException.class)
    public void shouldThrowExceptionWhenFileUploadFails() throws Exception {
        String subject = "SERVICE_INVOICE";
        String s3fileNameWithKey = Event.SERVICE_INVOICE.name() + "/" + subject;
        String emailBody =
                new TestMailUtils()
                        .createStringifiedEmail(
                                subject,
                                "emails/serviceInvoice.json",
                                "pdfs/CVBUServiceInvoice.pdf",
                                senderEmail);

        mockS3GetObjectReturnValue(s3fileNameWithKey, emailBody);

        S3FileContentRequest request = new S3FileContentRequest(emailBucketName, s3fileNameWithKey);

        Mockito.when(s3Client.putObject(any()))
                .thenThrow(new SdkClientException("S3 encountered error"));

        emailProcessorService.processS3Email(request);
        Mockito.verify(s3Client, Mockito.times(1)).getObject(any());
        Mockito.verify(s3Client, Mockito.times(1)).putObject(any());
    }
}
