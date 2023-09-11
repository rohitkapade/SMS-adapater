package com.tml.uep.config.aws.s3;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class S3Config {

    @Value("${aws.s3.region}")
    private String awsRegion;

    @Bean
    @Primary
    public AmazonS3 getAmazonS3Client() {
        Regions clientRegion = Regions.fromName(awsRegion);
        return AmazonS3ClientBuilder.standard().withRegion(clientRegion).build();
    }
}
