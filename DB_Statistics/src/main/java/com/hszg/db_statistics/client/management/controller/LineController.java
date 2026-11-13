package com.hszg.db_statistics.client.management.controller;

import com.hszg.db_statistics.client.management.service.DbManagementApiClient;
import com.hszg.db_statistics.client.management.dto.LineDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stations/")
@RequiredArgsConstructor
@Tag(name = "Lines", description = "Methods for getting Line data")
public class LineController {

    private final DbManagementApiClient apiClient;

    @GetMapping("{stationEva}/lines")
    @PreAuthorize("hasAuthority('READ_STATISTICS')")
    @Operation(summary = "Get all lines of one Station", description = "Retrieves a list of all lines at the station.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all lines at the station retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LineDto.class))),
            @ApiResponse(responseCode = "404", description = "Station not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<LineDto> getAllStationLines(
            @Parameter(description = "The EVA number of the station", required = true, example = "8010131")
            @PathVariable int stationEva
    ) {
        return ResponseEntity.ok(apiClient.getAllStationLines(stationEva));
    }

    @GetMapping("/lines")
    @PreAuthorize("hasAuthority('READ_STATISTICS')")
    @Operation(summary = "Get all lines", description = "Retrieves a list of all lines.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all lines retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LineDto[].class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<LineDto[]> getAllLines() {
        return ResponseEntity.ok(apiClient.getAllLines());
    }
}
