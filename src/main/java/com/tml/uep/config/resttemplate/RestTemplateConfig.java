package com.tml.uep.config.resttemplate;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    private static final long TIMEOUT_MILLIS = 10000;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.of(TIMEOUT_MILLIS, MILLIS))
                .setReadTimeout(Duration.of(TIMEOUT_MILLIS, MILLIS))
                .errorHandler(new RestTemplateExceptionHandler())
                .build();
    }
}
