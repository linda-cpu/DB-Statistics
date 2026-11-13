package com.hszg.DB_Management.AiData.API;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseAiDataDto {

    @JsonProperty("eva")
    @Schema(description = "The 7-digit EVA station number (Deutsche Bahn station ID)", example = "8000124", type = "integer")
    private int stationEva;

    @Schema(description = "The line to which the trip belongs", example = "S1", nullable = true)
    private String line;

    @Schema(description = "The time of departure in minutes since midnight calculated from the departure time", example = "120", type = "integer")
    private int minutesOfDay;
    
    @Schema(description = "The day of the week (1 = Monday, 7 = Sunday)", example = "3", type = "integer")
    private int dayOfWeek;

    @Schema(description = "Flag indicating if the current date falls within a school holiday period in the station's federal state", example = "false")
    private boolean schoolHoliday;

    @Schema(description = "Flag indicating if the current date is a public holiday in the station's federal state", example = "false")
    private boolean publicHoliday;

    @Schema(description = "Flag indicating active strike conditions impacting this trip (triggered by DB codes 09 or 52)", example = "false")
    private boolean strike;

    @Schema(description = "Average temperature in the last hours in °C", example = "16.4")
    private double avgTemperature;

    @Schema(description = "Average humidity in the last hours in dicimal", example = "16.4")
    private double avgHumidity;

    @Schema(description = "Sum of rain in the last hours in mm", example = "2.4")
    private double totalRain;

    @Schema(description = "Sum of snowfall in the last hours in mm", example = "2.4")
    private double totalSnowfall;
    
    @Schema(description = "Maximum wind gusts in the last hours in km/h", example = "45.0")
    private double maxWindGusts;

    @Schema(description = "Average wind speed in the last hours in km/h", example = "25.0")
    private double avgWindSpeed;

    @Schema(description = "Average wind speed in the last hours in W/m^2", example = "603.3")
    private double avgRadiation;
}
