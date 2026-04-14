package com.hszg.db_statistics.client.dto.trips;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
public class TripScheduleDto {

    @Schema(description = "Planned arrival time", example = "2025-01-15T14:30:00Z")
    @JsonProperty("arrival_plan")
    private Instant arrivalPlan;

    @Schema(description = "Real arrival time", example = "2025-01-15T14:32:00Z")
    @JsonProperty("arrival_real")
    private Instant arrivalReal;

    @Schema(description = "Planned departure time", example = "2026-01-15T14:35:00Z")
    @JsonProperty("depature_plan")
    private Instant departurePlan;

    @Schema(description = "Real departure time", example = "2026-01-15T14:36:00Z")
    @JsonProperty("depature_real")
    private Instant departureReal;

    @Schema(description = "Planned platform", example = "5")
    @JsonProperty("plattform_plan")
    private String platformPlan;

    @Schema(description = "Real platform", example = "6")
    @JsonProperty("plattform_real")
    private String platformReal;

}