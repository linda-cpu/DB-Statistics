package com.hszg.db_statistics.client.dto.stations;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Creating a new Station")
public class CreateStationDto {

    @Schema(description = "Name of the Station", example = "Görlitz")
    private String stationName;

    @Schema(description = "EVA-Number of the Station", example = "8010131")
    private int stationEva; 
}