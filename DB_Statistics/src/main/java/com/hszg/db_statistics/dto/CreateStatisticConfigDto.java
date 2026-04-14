package com.hszg.db_statistics.dto;

import com.hszg.db_statistics.enums.ChartType;
import com.hszg.db_statistics.enums.MetricType;
import com.hszg.db_statistics.enums.TimeInterval;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateStatisticConfigDto {
    @Schema(description = "Title of the configuration", example = "History of RE1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private ChartType chartType;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "8010131")
    private int stationEva;

    @Schema(example = "RE1")
    private String lineFilter;
    private MetricType metricType;
    private TimeInterval timeInterval;

    @Schema(example = "2026-01-01T00:00:00Z")
    private Instant dateFrom;

    @Schema(example = "2026-01-28T23:59:59Z")
    private Instant dateTo;

    @Schema(example="5")
    private Integer limit;
}