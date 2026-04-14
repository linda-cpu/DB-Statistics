package com.hszg.db_statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "represents a statistic with a label and a value")
public class StatisticDto {
    @Schema(description = "Label of the statistic", example = "22")
    private String label;

    @Schema(description = "The calculated value", example = "4.5")
    private double value;

    @Schema(description = "Description of the label.")
    private String description;

    public StatisticDto(String label, double value) {
        this.label = label;
        this.value = value;
        this.description = null;
    }
}
