package com.tml.uep.model.s3;

import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class S3FileContent {
    private byte[] content;
    private ObjectMetadata objectMetadata;
}
