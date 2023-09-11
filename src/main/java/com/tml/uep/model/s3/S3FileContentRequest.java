package com.tml.uep.model.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class S3FileContentRequest {
    private String s3BucketName;
    private String fileName;
}
