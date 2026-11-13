package com.hszg.db_statistics.client.management.controller;

import com.hszg.db_statistics.client.management.dto.strikes.CreateStrikeDto;
import com.hszg.db_statistics.client.management.dto.strikes.StrikeDto;
import com.hszg.db_statistics.client.management.service.DbManagementApiClient;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/strikes")
@RequiredArgsConstructor
@Tag(name = "Strikes", description = "Methods for fetching strikes data")
public class StrikeController {

    private final DbManagementApiClient apiClient;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_STRIKE')")
    @Operation(summary = "Get all strikes", description = "Retrieves a list of strikes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of strikes retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StrikeDto[].class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<StrikeDto[]> getAllStrikes() {
        return ResponseEntity.ok(apiClient.getStrikes());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_STRIKE')")
    @Operation(summary = "Create Strike", description = "Create a new Strike.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Strike created successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StrikeDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied.", content = @Content)
    })
    public ResponseEntity<StrikeDto> createStrike(@RequestBody CreateStrikeDto strikeDto) {
        StrikeDto createdStrike = apiClient.createStrike(strikeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStrike);
    }
}
