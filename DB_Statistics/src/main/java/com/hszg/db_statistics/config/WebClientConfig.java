package com.hszg.db_statistics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.db-management-service.url}")
    private String baseUrl;

    @Value("${app.db-management.api-key}")
    private String apiKey;

    @Value("${app.ml-service.url:http://193.174.103.35:5000}")
    private String mlServiceUrl;

    @Bean
    public WebClient dbManagementWebClient() {
        final int size = 16 * 1024 * 1024;
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("SERVICE-API-KEY", apiKey)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(size))
                .build();
    }

    @Bean
    public WebClient dbMlWebClient() {
        return WebClient.builder()
                .baseUrl(mlServiceUrl)
                .build();
    }
}