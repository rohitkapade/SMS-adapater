package com.tml.uep.model.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class S3FileCreationRequest {
    private String fileName;
    private String s3BucketName;
    private byte[] fileData;
    private String contentType;
    private String s3Key;
    private boolean returnSignedUrl;
}
