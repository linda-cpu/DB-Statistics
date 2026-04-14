package com.hszg.db_statistics.client.dto.stations;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Station Data")
public class StationDto {

    @JsonAlias("stationName")
    @JsonProperty("name")
    @Schema(description = "Name of the Station", example = "Görlitz")
    private String stationName;

    @JsonAlias("stationEva")
    @JsonProperty("eva")
    @Schema(description = "EVA-Number of the Station", example = "8010131")
    private int stationEva;
}
