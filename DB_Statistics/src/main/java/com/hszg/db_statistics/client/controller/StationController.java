package com.hszg.db_statistics.client.controller;

import com.hszg.db_statistics.client.service.DbManagementApiClient;
import com.hszg.db_statistics.client.dto.stations.CreateStationDto;
import com.hszg.db_statistics.client.dto.stations.StationDto;
import com.hszg.db_statistics.client.dto.stations.StationPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
@Tag(name = "Stations", description = "Methods for fetching station data")
public class StationController {

    private final DbManagementApiClient apiClient;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_STATION')")
    @Operation(summary = "Get all stations (paged)", description = "Retrieves a paginated list of stations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of stations retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StationPageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<StationPageResponse> getAllStations(
            @Parameter(description = "Page number (starting from 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of stations per page", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(apiClient.getAllStations(page, size));
    }

    @GetMapping("/{eva}")
    @PreAuthorize("hasAuthority('READ_STATION')")
    @Operation(summary = "Get specific station details", description = "Retrieves public information about a specific station.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Station found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StationDto.class))),
            @ApiResponse(responseCode = "404", description = "Station not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<StationDto> getStation(
        @Parameter(description = "EVA number of the station", example = "8010131")
        @PathVariable int eva
    ) {
        return ResponseEntity.ok(apiClient.getStationByEVA(eva));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_STATION')")
    @Operation(summary = "Create Station", description = "Create a new Station.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Station created successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StationDto.class))),
            @ApiResponse(responseCode = "409", description = "Station with given EVA number already exists.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Station not found.", content = @Content)
    })
    public ResponseEntity<StationDto> createStation(@RequestBody CreateStationDto stationDto) {
        StationDto createdStation = apiClient.createStation(stationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStation);
    }
}
