package com.hszg.db_statistics.client.dto.trips;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hszg.db_statistics.client.dto.annotations.AnnotationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class TripDto {

    @Schema(description = "Unique identifier for the trip", example = "-6420897253859089585-2512182347-23")
    @JsonProperty("id")
    private String tripId;

    @Schema(description = "Station name", example = "Görlitz")
    @JsonProperty("station")
    private String station;

    @JsonProperty("schedule")
    private TripScheduleDto schedule;

    @JsonProperty("train_info")
    private TripTrainInfoDto trainInfo;

    @JsonProperty("annotations")
    private List<AnnotationDto> annotations;
}