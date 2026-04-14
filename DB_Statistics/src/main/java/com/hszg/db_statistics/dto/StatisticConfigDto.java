package com.hszg.db_statistics.dto;

import com.hszg.db_statistics.enums.ChartType;
import com.hszg.db_statistics.enums.MetricType;
import com.hszg.db_statistics.enums.TimeInterval;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Schema(description = "Saved Statistic Configuration")
public class StatisticConfigDto {
    private Long id;

    @Schema(example = "My Delay History Chart")
    private String title;

    @Schema(example = "DELAY_HISTORY")
    private ChartType chartType;

    @Schema(example = "8010131")
    private int stationEva;

    @Schema(example = "RE1")
    private String lineFilter;

    @Schema(example = "PERCENT")
    private MetricType metricType;

    @Schema(example = "DAILY")
    private TimeInterval timeInterval;

    @Schema(example = "2026-01-01T00:00:00Z")
    private Instant dateFrom;

    @Schema(example = "2026-01-28T23:59:59Z")
    private Instant dateTo;

    @Schema(example = "5")
    private Integer limit;
}