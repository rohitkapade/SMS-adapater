package com.tml.uep.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.s3.S3FileContent;
import com.tml.uep.model.s3.S3FileContentRequest;
import com.tml.uep.model.s3.S3FileCreationRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class S3FileService {

    private final AmazonS3 s3Client;
    private final String signedUrlExpiryMinutes;

    public S3FileService(
            AmazonS3 s3Client,
            @Value("${aws.s3.signed-url-expiry-mins}") String signedUrlExpiryMinutes) {
        this.s3Client = s3Client;
        this.signedUrlExpiryMinutes = signedUrlExpiryMinutes;
    }

    public String uploadFile(S3FileCreationRequest s3File, Map<String, String> fileContextMap) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(s3File.getContentType());

            byte[] fileContent = s3File.getFileData();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileContent);
            metadata.setContentLength(fileContent.length);
            fileContextMap.forEach(metadata::addUserMetadata);

            String filePath = Path.of(s3File.getS3Key(), s3File.getFileName()).toString();
            PutObjectRequest request =
                    new PutObjectRequest(
                            s3File.getS3BucketName(), filePath, byteArrayInputStream, metadata);
            log.info("Uploading file: {} to S3 bucket: {}", filePath, s3File.getS3BucketName());
            s3Client.putObject(request);

            if (s3File.isReturnSignedUrl()) {
                return getSignedS3FileUrl(
                        new S3FileContentRequest(s3File.getS3BucketName(), filePath));
            } else {
                return s3Client.getUrl(s3File.getS3BucketName(), s3File.getS3Key()).toString();
            }
        } catch (SdkClientException e) {
            String errorMsg =
                    "Amazon S3 couldn't process file upload or couldn't parse the response from Amazon S3";
            log.error(errorMsg, e);
            throw new ExternalSystemException(errorMsg, e);
        }
    }

    public S3FileContent getFileData(S3FileContentRequest request) {
        log.info(
                "Downloading file {} from bucket {}",
                request.getFileName(),
                request.getS3BucketName());
        try (S3Object s3Object =
                s3Client.getObject(
                        new GetObjectRequest(request.getS3BucketName(), request.getFileName()))) {
            byte[] content = s3Object.getObjectContent().readAllBytes();
            return new S3FileContent(content, s3Object.getObjectMetadata());
        } catch (SdkClientException | IOException e) {
            String errorMsg =
                    "Amazon S3 couldn't download file or couldn't parse the response from Amazon S3";
            log.error(errorMsg, e);
            throw new ExternalSystemException(errorMsg, e);
        }
    }

    public String getSignedS3FileUrl(S3FileContentRequest s3FileContentRequest) {
        String signedUrl;
        try {
            // Set the pre-signed URL to expire after one day.
            java.util.Date expiration = new java.util.Date();
            long expTimeMillis = Instant.now().toEpochMilli();
            expTimeMillis += (1000 * 60 * Integer.parseInt(signedUrlExpiryMinutes));
            expiration.setTime(expTimeMillis);

            // Generate the pre-signed URL.
            log.info("Generating pre-signed URL for file : {}", s3FileContentRequest.getFileName());
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(
                                    s3FileContentRequest.getS3BucketName(),
                                    s3FileContentRequest.getFileName())
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
            signedUrl = url.toString();
            log.info("Pre-Signed URL: " + signedUrl);
        } catch (SdkClientException e) {
            String errorMsg = "Error occurred while obtaining signed S3 url";
            log.error(errorMsg, e);
            throw new ExternalSystemException(errorMsg, e);
        }
        return signedUrl;
    }
}
