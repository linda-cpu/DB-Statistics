package com.hszg.db_statistics.client.ml.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response from the ML delay prediction service")
public class PredictionResponseDto {

    @JsonAlias("predicted_delay_minutes") // <-- HIER: Mapping für Python's snake_case
    @Schema(description = "The predicted delay in minutes", example = "4.55")
    private Double predictedDelayMinutes;

    @JsonAlias("requested_input")         // <-- HIER: Mapping für Python's snake_case
    @Schema(description = "The raw request input echoed back by the service")
    private PredictionRequestDto requestedInput;

    @Schema(description = "Status of the prediction", example = "success")
    private String status;
}