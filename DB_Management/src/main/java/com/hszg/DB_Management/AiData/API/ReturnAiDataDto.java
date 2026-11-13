package com.hszg.DB_Management.AiData.API;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.bson.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hszg.DB_Management.Trip.Database.TripStopEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data transfer object for AI training data, containing relevant trip information and delay details.")
public class ReturnAiDataDto extends BaseAiDataDto {

    @Schema(description = "Unique identifier of the trip stop", example = "-8203578369052827954-2512011817-11")
    private String id;

    @JsonProperty("delay")
    @Schema(description = "The measured delay in minutes. Behaviors are prioritized as: Departure Delay -> Arrival Delay -> 0", example = "5", type = "integer")
    private int departureDelayMinutes;


    public ReturnAiDataDto() {

    }

    public static ReturnAiDataDto of(TripStopEntity entity) {
        if (entity == null) return null;
             
        if (entity.getDeparturePlan() == null) return null;

        ReturnAiDataDto baseDto = new ReturnAiDataDto();

        baseDto.setId(entity.getId());
        baseDto.setStationEva(entity.getStationEva());
        baseDto.setLine(entity.getLine());

        ZonedDateTime departureZoned = entity.getDeparturePlan().atZone(ZoneId.of("Europe/Berlin"));
        baseDto.setMinutesOfDay(departureZoned.getHour()*60 + departureZoned.getMinute());
        baseDto.setDayOfWeek(entity.getDayOfWeek().getValue());

        if (entity.getCancellationTime() != null) {
            baseDto.setDepartureDelayMinutes(130);
        }
        else if (entity.getDepartureReal() != null) {
            long delayMinutes = java.time.Duration.between(entity.getDeparturePlan(), entity.getDepartureReal()).toMinutes();
            setDelay(baseDto, delayMinutes);
        } 
        else if(entity.getArrivalReal() != null) {
            long delayMinutes = java.time.Duration.between(entity.getArrivalPlan(), entity.getArrivalReal()).toMinutes();
            setDelay(baseDto, delayMinutes);
        } 
        else {
            baseDto.setDepartureDelayMinutes(0);
        } 

        if (entity.getAnnotations() != null) {
            baseDto.setStrike(entity.getAnnotations().stream()
                    .anyMatch(annotation -> annotation.getCode() == 9 || annotation.getCode() == 52));
        }

        baseDto.setSchoolHoliday(entity.getSchoolHoliday() != null && entity.getSchoolHoliday());
        baseDto.setPublicHoliday(entity.getPublicHoliday() != null && entity.getPublicHoliday());
        
        return baseDto;
    }

    private static void setDelay(ReturnAiDataDto baseDto, long delayMinutes) {
        if (delayMinutes > 120) {
            baseDto.setDepartureDelayMinutes(120);
            //System.out.println("big delay: " + delayMinutes);
        }
        else {
            baseDto.setDepartureDelayMinutes((int) delayMinutes);
        }
    }


    // Hilfsmethode, um Typkonflikte (z.B. Int/Decimal128 zu Double) zu vermeiden
    public void mapWeatherFields(Document weatherDoc) {
        if (weatherDoc == null) return;
        this.setAvgTemperature(getAsDouble(weatherDoc, "avgTemperature"));
        this.setAvgHumidity(getAsDouble(weatherDoc, "avgHumidity"));
        this.setTotalRain(getAsDouble(weatherDoc, "totalRain"));
        this.setTotalSnowfall(getAsDouble(weatherDoc, "totalSnowfall"));
        this.setMaxWindGusts(getAsDouble(weatherDoc, "maxWindGusts"));
        this.setAvgWindSpeed(getAsDouble(weatherDoc, "avgWindSpeed"));
        this.setAvgRadiation(getAsDouble(weatherDoc, "avgRadiation"));
    }

    private double getAsDouble(Document doc, String key) {
        Object val = doc.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return 0.0;
    }
}
