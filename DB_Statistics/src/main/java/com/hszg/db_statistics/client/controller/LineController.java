package com.hszg.db_statistics.client.controller;

import com.hszg.db_statistics.client.service.DbManagementApiClient;
import com.hszg.db_statistics.client.dto.LineDto;
import com.hszg.db_statistics.client.dto.stations.StationPageResponse;
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
@RequestMapping("/api/stations/{stationEva}/lines")
@RequiredArgsConstructor
@Tag(name = "Lines", description = "Methods for getting Line data")
public class LineController {

    private final DbManagementApiClient apiClient;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_STATISTICS')")
    @Operation(summary = "Get all lines (paged)", description = "Retrieves a paginated list of all lines at the station.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all lines at the station retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StationPageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Station not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<LineDto> getAllLines(
            @Parameter(description = "The EVA number of the station", required = true, example = "8010131")
            @PathVariable int stationEva
    ) {
        return ResponseEntity.ok(apiClient.getAllLines(stationEva));
    }
}
