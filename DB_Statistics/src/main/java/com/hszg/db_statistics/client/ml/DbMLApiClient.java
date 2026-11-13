package com.hszg.db_statistics.client.ml;

import com.hszg.db_statistics.client.ml.dto.PredictionRequestDto;
import com.hszg.db_statistics.client.ml.dto.PredictionResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbMLApiClient {

    private final WebClient dbMlWebClient;

    public PredictionResponseDto predictDelay(PredictionRequestDto requestDto) {
        log.info("Sending prediction request to ML service for station: {}, line: {}",
                requestDto.getEva(), requestDto.getLine());

        try {
            return dbMlWebClient
                    .post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(PredictionResponseDto.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("ML Service error: {} - Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            // Hier werfen wir unsere neue Exception mit dem originalen Body
            throw new MlServiceException(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error connecting to ML Service", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to connect to prediction service.");
        }
    }
}