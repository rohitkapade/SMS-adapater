package com.tml.uep.config.resttemplate;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SolrRestTemplateConfig {
    private static final long TIMEOUT_MILLIS = 10000;

    @Value("${solr-api.base-url}")
    private String SOLR_API_BASE_URL;

    @Value("${solr-api.auth-token}")
    private String AUTH_TOKEN;

    @Bean(name = "solrRestTemplate")
    public RestTemplate solarRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri(SOLR_API_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_TOKEN)
                .setConnectTimeout(Duration.of(TIMEOUT_MILLIS, MILLIS))
                .setReadTimeout(Duration.of(TIMEOUT_MILLIS, MILLIS))
                .errorHandler(new RestTemplateExceptionHandler())
                .build();
    }
}
