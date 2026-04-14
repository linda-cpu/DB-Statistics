package com.hszg.db_statistics.client.service;

import com.hszg.db_statistics.auth.AuthenticationService;
import com.hszg.db_statistics.client.dto.DelayReasonDto;
import com.hszg.db_statistics.client.dto.LineDto;
import com.hszg.db_statistics.client.dto.annotations.AnnotationDto;
import com.hszg.db_statistics.client.dto.annotations.CreateAnnotationDto;
import com.hszg.db_statistics.client.dto.annotations.GetAnnotationDto;
import com.hszg.db_statistics.client.dto.stations.CreateStationDto;
import com.hszg.db_statistics.client.dto.stations.StationDto;
import com.hszg.db_statistics.client.dto.stations.StationPageResponse;
import com.hszg.db_statistics.client.dto.trips.TripDto;
import com.hszg.db_statistics.client.dto.trips.TripPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbManagementApiClient {
    private final WebClient dbManagementWebClient;
    private final AuthenticationService authenticationService;

    @Value("${app.db-management.api-key}")
    private String apiKey;

    public StationPageResponse getAllStations(int page, int size) {
        log.info("load stations (page {}, size {})", page, size);
        try {
            return dbManagementWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/stations")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .bodyToMono(StationPageResponse.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Error at loading stations: {}", e.getStatusCode());
            return new StationPageResponse();
        }
    }

    public StationDto getStationByEVA(int eva) {
        log.info("getStationByEVA");
        try {
            ResponseEntity<StationDto> response  = dbManagementWebClient
                    .get()
                    .uri("/stations/{eva}", eva)
                    .retrieve()
                    .toEntity(StationDto.class)
                    .block();
            return response.getBody();
        } catch (WebClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            switch (status) {
                case HttpStatus.NOT_FOUND -> {
                    log.warn("Station with this EVA number does not exist at DB.");
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Station with this EVA number does not exist at DB.");
                }
                case HttpStatus.BAD_REQUEST -> {
                    log.warn("Invalid inputs (Name or EVA missing).");
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid inputs (Name or EVA missing).");
                }
                case HttpStatus.UNAUTHORIZED -> {
                    log.error("CRITICAL: Upstream authentication misconfigured!");
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal configuration error.");
                }
                default -> {
                    log.error("Unhandled error from upstream API: {}", status);
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream API error.");
                }
            }
        }
    }

    public StationDto createStation(CreateStationDto stationDto) {
        log.info("Send create request for station: {}", stationDto);
        try {
           return dbManagementWebClient
                    .post()
                    .uri("/stations")
                    .bodyValue(stationDto)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(StationDto.class)
                    .block();

        } catch (WebClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();

            log.error("Error from upstream API when creating the station: {} - {}", status, e.getResponseBodyAsString());

            switch (status) {
                case HttpStatus.CONFLICT -> {
                    log.warn("Station with this EVA number already exists.");
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Station already exists with this EVA number.");
                }
                case HttpStatus.NOT_FOUND -> {
                    log.warn("Station with this EVA number does not exist at DB.");
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Station with this EVA number does not exist at DB.");
                }
                case HttpStatus.BAD_REQUEST -> {
                    log.warn("Invalid inputs (Name or EVA missing).");
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid inputs (Name or EVA missing).");
                }
                case HttpStatus.UNAUTHORIZED -> {
                    log.error("CRITICAL: Upstream authentication misconfigured!");
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal configuration error.");
                }
                default -> {
                    log.error("Unhandled error from upstream API: {}", status);
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream API error.");
                }
            }
        }
    }

    public AnnotationDto createAnnotation(int stationEva, String tripId, CreateAnnotationDto annotationDto) {
        log.info("Create annotation for trip {}", tripId);
        annotationDto.setServiceId(apiKey);

        try {
            return dbManagementWebClient
                    .post()
                    .uri("/stations/{stationEva}/trips/{tripId}/annotations", stationEva, tripId)
                    .bodyValue(annotationDto)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(AnnotationDto.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Error: {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

    public AnnotationDto updateAnnotation(int stationEva, String tripId, String annotationId, CreateAnnotationDto annotationDto) {
        log.info("Update Annotation {} (Station: {}, Trip: {})", annotationId, stationEva, tripId);
        annotationDto.setServiceId(apiKey);
        try {
            return dbManagementWebClient
                    .patch()
                    .uri("/stations/{stationEva}/trips/{tripId}/annotations/{annotationId}", stationEva, tripId, annotationId)
                    .bodyValue(annotationDto)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(AnnotationDto.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Error: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(e.getStatusCode(), "Upstream-Fehler: " + e.getResponseBodyAsString());
        }
    }

    public void deleteAnnotation(int stationEva, String tripId, String annotationId) {
        log.info("Delete Annotation {} (Trip: {})", annotationId, tripId);

        try {
            dbManagementWebClient
                    .delete()
                    .uri("/stations/{stationEva}/trips/{tripId}/annotations/{annotationId}", stationEva, tripId, annotationId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Error deleting annotation: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

    public TripPageResponse getTrips(int stationEva, String line, Instant from, Instant to,
                                     int page, int size) {
        log.info("Load trips: Eva={}, Page={}, Sort={} ({})", stationEva, page);

        try {
            TripPageResponse response = dbManagementWebClient
                    .get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/stations/{stationEva}/trips")
                                .queryParam("page", page)
                                .queryParam("size", size);

                        if (line != null && !line.isBlank()) builder.queryParam("line", line);
                        if (from != null) builder.queryParam("from", from);
                        if (to != null) builder.queryParam("to", to);

                        return builder.build(stationEva);
                    })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(TripPageResponse.class)
                    .block();

            return (response != null) ? response : new TripPageResponse();
        } catch (WebClientResponseException e) {
            log.error("Error loading trips: {}", e.getStatusCode());
            throw e;
        }
    }

    public TripDto getTripById(int eva, String tripId) {
        log.info("getTripById");
        try {
            return dbManagementWebClient
                    .get()
                    .uri("/stations/{stationEva}/trips/{tripId}", eva, tripId)
                    .retrieve()
                    .bodyToMono(TripDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("getTripById WebClientResponseException", e);
            throw new RuntimeException(e);
        }
    }

    public DelayReasonDto getDelayReason(int code) {
        log.info("Load delay reason for code {}", code);
        try {
            return dbManagementWebClient
                    .get()
                    .uri("/delayReason/{code}", code)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(DelayReasonDto.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Error loading delay reason: {}", e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delay reason not found: " + code);
            }
            throw e;
        }
    }

    public LineDto getAllLines(int stationEva) {
        log.info("getAllLines");
        try {
            return dbManagementWebClient
                    .get()
                    .uri("/stations/{stationEva}/lines", stationEva)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(LineDto.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Error loading lines: {}", e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Station not found: " + stationEva);
            }
            throw e;
        }
    }

    public GetAnnotationDto[] getAnnotations(int stationEva, Long userId) {
        return dbManagementWebClient
                .get()
                .uri("/stations/{stationEva}/trips/{serviceId}/{userId}/annotations", stationEva, apiKey, userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono((GetAnnotationDto[].class))
                .block();
    }
}