package com.hszg.db_statistics.controller;

import com.hszg.db_statistics.dto.CreateStatisticConfigDto;
import com.hszg.db_statistics.dto.StatisticConfigDto;
import com.hszg.db_statistics.service.StatisticConfigService;
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

import java.util.List;

@RestController
@RequestMapping("/api/statistics/configs")
@RequiredArgsConstructor
@Tag(name = "Statistic Favorites", description = "Manage personal dashboard configurations")
public class StatisticConfigController {

    private final StatisticConfigService configService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_FAVORITES')")
    @Operation(summary = "Get my favorites", description = "Returns all saved statistic configurations for the logged-in user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of configurations", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticConfigDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content),
    })
    public ResponseEntity<List<StatisticConfigDto>> getMyConfigs() {
        return ResponseEntity.ok(configService.getMyConfigs());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_FAVORITES')")
    @Operation(summary = "Save new favorite", description = "Creates a new configuration preset.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticConfigDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content),
    })
    public ResponseEntity<StatisticConfigDto> createConfig(@RequestBody CreateStatisticConfigDto dto) {
        return ResponseEntity.ok(configService.createConfig(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_FAVORITES')")
    @Operation(summary = "Update favorite", description = "Updates an existing configuration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatisticConfigDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content),
            @ApiResponse(responseCode = "404", description = "Configuration not found", content = @Content),
    })
    public ResponseEntity<StatisticConfigDto> updateConfig(
            @Parameter(description = "ID of the configuration to update", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody CreateStatisticConfigDto dto) {
        return ResponseEntity.ok(configService.updateConfig(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_FAVORITES')")
    @Operation(summary = "Delete favorite", description = "Removes a configuration preset.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Configuration deleted successfully", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content),
            @ApiResponse(responseCode = "404", description = "Configuration not found", content = @Content),
    })
    public ResponseEntity<Void> deleteConfig(
            @Parameter(description = "ID of the configuration to delete", required = true, example = "1")
            @PathVariable Long id
    ) {
        configService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }
}