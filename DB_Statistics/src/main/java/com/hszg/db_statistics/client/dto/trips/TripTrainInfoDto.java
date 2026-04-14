package com.hszg.db_statistics.client.dto.trips;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TripTrainInfoDto {

    @Schema(description = "Train number", example = "RE1")
    @JsonProperty("line")
    private String line;

    @Schema(description = "Train type", example = "-6420897253859089585")
    @JsonProperty("id")
    private String id;
}