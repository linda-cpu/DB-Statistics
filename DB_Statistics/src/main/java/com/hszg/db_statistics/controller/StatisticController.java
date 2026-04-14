package com.hszg.db_statistics.controller;

import com.hszg.db_statistics.dto.StatisticDto;
import com.hszg.db_statistics.enums.MetricType;
import com.hszg.db_statistics.enums.TimeInterval;
import com.hszg.db_statistics.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Statistic Endpoints")
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/stations/{stationEva}/history/delays")
    @PreAuthorize("hasAuthority('READ_DATA')")
    @Operation(summary = "Calculate Delay History", description = "Gets the delay history for a station over time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of delay history", 
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = StatisticDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content)
    })
    public ResponseEntity<List<StatisticDto>> getDelayHistory(
            @Parameter(description = "The EVA number of the station", required = true, example = "8010131")
            @PathVariable int stationEva,

            @Parameter(description = "Filter by line", example = "RE1")
            @RequestParam(required = false) String line,

            @Parameter(description = "Filter by departure time (from)", example = "2026-01-01T00:00:00Z")
            @RequestParam(required = false) Instant from,

            @Parameter(description = "Filter by departure time (to)", example = "2026-01-29T23:59:59Z")
            @RequestParam(required = false) Instant to,

            @Parameter(description = "Metric type", example = "PERCENT")
            @RequestParam(defaultValue = "PERCENT") MetricType type,

            @Parameter(description = "Time interval for aggregation", example = "DAILY")
            @RequestParam(defaultValue = "DAILY") TimeInterval interval
    ) {
        return ResponseEntity.ok(statisticService.getDelaySeries(stationEva, line, from, to, type, interval));
    }

    @GetMapping("/stations/{stationEva}/top-delays")
    @PreAuthorize("hasAuthority('READ_DATA')")
    @Operation(summary = "Top Delayed Lines", description = "Gets the top delayed lines for a station.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of top delayed lines",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = StatisticDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "403", description = "", content = @Content)
    })
    public ResponseEntity<List<StatisticDto>> getTopDelayedLines(
            @Parameter(description = "The EVA number of the station", required = true, example = "8010131")
            @PathVariable int stationEva,

            @Parameter(description = "Filter by departure time (from)", example = "2026-01-01T00:00:00Z")
            @RequestParam(required = false) Instant from,

            @Parameter(description = "Filter by departure time (to)", example = "2026-01-29T23:59:59Z")
            @RequestParam(required = false) Instant to,

            @Parameter(description = "Number of top lines to return", example = "5")
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(statisticService.getTopDelayedLines(stationEva, from, to, limit));
    }

    @GetMapping("/stations/{stationEva}/top-reasons")
    @PreAuthorize("hasAuthority('READ_DATA')")
    @Operation(summary = "Top Delay Reasons", description = "Gets the top delay reasons for a station (excluding codes 98 and 0).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of top delay reasons",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = StatisticDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content)
    })
    public ResponseEntity<List<StatisticDto>> getTopDelayReasons(
            @Parameter(description = "The EVA number of the station", required = true, example = "8010131")
            @PathVariable int stationEva,

            @Parameter(description = "Filter by departure time (from)", example = "2026-01-01T00:00:00Z")
            @RequestParam(required = false) Instant from,

            @Parameter(description = "Filter by departure time (to)", example = "2026-01-29T23:59:59Z")
            @RequestParam(required = false) Instant to,

            @Parameter(description = "Number of top reasons to return", example = "5")
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(statisticService.getTopDelayReasons(stationEva, from, to, limit));
    }
}