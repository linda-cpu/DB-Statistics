package com.hszg.DB_Management.Trip.Api.Dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Helper class to create GetTripDto.")
@Getter
@Setter
public class Schedule {
    @JsonProperty("arrival_plan")
	private Instant arrivalPlan;
	
    @Nullable
    @JsonProperty("arrival_real")
	private Instant arrivalReal;
	
    @JsonProperty("depature_plan")
	private Instant departurePlan;
	
    @Nullable
    @JsonProperty("depature_real")
	private Instant departureReal;
	
    @JsonProperty("plattform_plan")
	private int plattformPlan;
	
    @Nullable
    @JsonProperty("plattform_real")
	private Integer plattformReal;
 
}
