package com.tml.uep.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.Event;
import com.tml.uep.model.s3.S3FileCreationRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@Import(AWSTestConfiguration.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class S3FileContentServiceTest {

    @Value("${aws.s3.outbound-document-bucket-name}")
    private String outboundDocumentBucketName;

    @Value("${aws.s3.signed-url-expiry-mins}")
    private String signedUrlExpiryMinutes;

    @MockBean private AmazonS3 s3Client;

    private S3FileService s3FileService;

    private String expectedS3PdfUrl =
            "https://engagex-mail-extracted-pdfs.s3.amazonaws.com/SERVICE_INVOICE/CVBUServiceInvoice.pdf";
    private String s3PdfFileNameWithKey = Event.SERVICE_INVOICE.name() + "/CVBUServiceInvoice.pdf";

    @Before
    public void setUpMockS3Client() {
        s3FileService = new S3FileService(s3Client, signedUrlExpiryMinutes);
        Mockito.reset(s3Client);
    }

    @Test
    public void shouldUploadFileToS3WithoutErrors() throws URISyntaxException, IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("pdfs/CVBUServiceInvoice.pdf");
        File file = new File(url.toURI());

        S3FileCreationRequest s3FileCreationRequest =
                new S3FileCreationRequest(
                        "CVBUServiceInvoice.pdf",
                        outboundDocumentBucketName,
                        new FileInputStream(file).readAllBytes(),
                        "application/pdf",
                        Event.SERVICE_INVOICE.name(),
                        true);
        HashMap<String, String> fileContextMap = new HashMap<>();

        Mockito.when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL(expectedS3PdfUrl));

        s3FileService.uploadFile(s3FileCreationRequest, fileContextMap);
        Mockito.verify(s3Client, times(1)).putObject(any());
    }

    @Test(expected = ExternalSystemException.class)
    public void shouldThrowExceptionWhenUploadingEncountersAnError()
            throws URISyntaxException, IOException {
        Mockito.when(s3Client.putObject(any()))
                .thenThrow(new SdkClientException("S3 encountered error"));

        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("pdfs/CVBUServiceInvoice.pdf");
        File file = new File(url.toURI());

        S3FileCreationRequest s3FileCreationRequest =
                new S3FileCreationRequest(
                        "CVBUServiceInvoice.pdf",
                        outboundDocumentBucketName,
                        new FileInputStream(file).readAllBytes(),
                        "application/pdf",
                        Event.SERVICE_INVOICE.name(),
                        true);
        HashMap<String, String> fileContextMap = new HashMap<>();

        s3FileService.uploadFile(s3FileCreationRequest, fileContextMap);
        Mockito.verify(s3Client, times(1)).putObject(any());
    }
}
