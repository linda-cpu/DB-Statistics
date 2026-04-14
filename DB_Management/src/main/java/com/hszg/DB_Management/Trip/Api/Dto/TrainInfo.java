package com.hszg.DB_Management.Trip.Api.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Helper class to create GetTripDto.")
@Getter
@Setter
public class TrainInfo {

    @JsonProperty("id")
    private String tripId;

    private String line; 

}
