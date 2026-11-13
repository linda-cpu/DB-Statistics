package com.hszg.db_statistics.client.management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lines of a station")
public class LineDto {

    @Schema(description = "Code of the station", example = "8010131")
    @JsonProperty("station_eva")
    private int eva;

    @Schema(description = "Name of the Line", example = "[\"RE1\", \"RB61\"]")
    private String[] lines;
}