package com.tml.uep.config.resttemplate;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OptyCreationRestTemplateConfig {
    private static final long TIMEOUT_MILLIS = 60000;

    @Value("${solr-api.base-url}")
    private String SOLR_API_BASE_URL;

    @Value("${solr-api.cv-opty-auth-token}")
    private String AUTH_TOKEN;

    @Autowired private RequestFactorySupplier requestFactorySupplier;

    @Bean(name = "optyCreationRestTemplate")
    public RestTemplate solarOptyRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri(SOLR_API_BASE_URL)
                .requestFactory(requestFactorySupplier)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_TOKEN)
                .setConnectTimeout(Duration.of(TIMEOUT_MILLIS, MILLIS))
                .setReadTimeout(Duration.of(TIMEOUT_MILLIS, MILLIS))
                .errorHandler(new OptyRestTemplateExceptionHandler())
                .build();
    }
}
