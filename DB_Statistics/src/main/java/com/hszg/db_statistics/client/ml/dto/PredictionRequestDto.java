package com.hszg.db_statistics.client.ml.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request parameters for ML delay prediction")
public class PredictionRequestDto {

    @NotNull(message = "dateTime is required")
    @Schema(description = "Planned departure time in UTC (ISO-8601 format). Must be in the future (max 16 days).", example = "2026-07-20T14:30:00Z")
    private Instant dateTime;

    @NotBlank(message = "eva is required")
    @Schema(description = "7-digit Deutsche Bahn station ID (EVA number)", example = "8010131")
    private String eva;

    @NotBlank(message = "line is required")
    @Schema(description = "Train line identifier", example = "RE1")
    private String line;
}