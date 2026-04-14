package com.hszg.db_statistics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.db-management-service.url}")
    private String baseUrl;

    @Value("${app.db-management.api-key}")
    private String apiKey;

    @Bean
    public WebClient dbManagementWebClient(
    ) {
        final int size = 16 * 1024 * 1024;

        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl
                )
                .defaultHeader("SERVICE-API-KEY", apiKey)
                .exchangeStrategies(strategies) // <--- HIER IST DER TRICK
                .build();
    }
}
