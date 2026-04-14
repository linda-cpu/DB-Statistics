package com.hszg.db_statistics.client.controller;

import com.hszg.db_statistics.client.service.DbManagementApiClient;
import com.hszg.db_statistics.client.dto.trips.TripDto;
import com.hszg.db_statistics.client.dto.trips.TripPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/stations/{stationEva}/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Get Trip Data")
public class TripController {

    private final DbManagementApiClient apiClient;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_DATA')")
    @Operation(summary = "Get all trips (pageable)", description = "Get all trips for a specific station with optional filters and pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of trips", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TripPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content)
    })
    public ResponseEntity<TripPageResponse> getTrips(
            @Parameter(description = "The EVA number of the station", required = true, example = "8010131")
            @PathVariable int stationEva,

            @Parameter(description = "Filter by line", example = "RE1")
            @RequestParam(required = false) String line,

            @Parameter(description = "Filter by departure time (from)", example = "2025-12-01T00:00:00Z")
            @RequestParam(required = false) Instant from,

            @Parameter(description = "Filter by departure time (to)", example = "2025-12-29T23:59:59Z")
            @RequestParam(required = false) Instant to,

            @Parameter(description = "Page number for pagination (default is 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size for pagination (default is 20)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(apiClient.getTrips(stationEva, line, from, to, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_DATA')")
    @Operation(summary = "Get trip by ID", description = "Get detailed information about a specific trip by its ID for a specific station.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful retrieval of trip",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TripDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid trip ID", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trip not found", content = @Content)
    })
    public ResponseEntity<TripDto> getTripById(
        @Parameter (description = "The EVA number of the station", required = true, example = "8010131")
        @PathVariable int stationEva,

        @Parameter(description = "The ID of the trip", required = true, example = "-6420897253859089585-2512182347-23")
        @PathVariable String id
    ) {
        return ResponseEntity.ok(apiClient.getTripById(stationEva, id));
    }
}