package com.hszg.DB_Management.Trip.Api.Dto;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hszg.DB_Management.Trip.Annotation.Api.Dto.AnnotationReturnDto;
import com.hszg.DB_Management.Trip.Database.TripStopBasicEntity;
import com.hszg.DB_Management.Trip.Database.TripStopEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dto used to return a trip.")
@Getter
@Setter
public class GetTripDto {

    private String id;

    @JsonProperty("station")
    private String stationName;

    @JsonProperty("train_info")
    private TrainInfo trainInfo;
    private Schedule schedule;
    private List<AnnotationReturnDto> annotations;



    /**
     * Static factory method to create a DTO from a Database Entity
     */
    public static GetTripDto of(TripStopEntity entity) {
        if (entity == null) return null;

        GetTripDto dto = new GetTripDto();
        
        // Root fields: Combine ID components as requested
        dto.setId(entity.getId());
        dto.setStationName(entity.getStationName());

        // Map Train Info
        TrainInfo info = new TrainInfo();
        info.setTripId(entity.getTripId());
        if (entity.getLine() != null && !entity.getLine().isBlank()) {
            info.setLine(entity.getLine());
        } /*else if (entity.getTrainCategory() != null && !entity.getTrainCategory().isBlank()) {
            info.setLine(entity.getTrainCategory().concat(String.valueOf(entity.getTrainNumber())));
        }*/
        dto.setTrainInfo(info);

        // Map Schedule
        Schedule schedule = new Schedule();
        schedule.setArrivalPlan(entity.getArrivalPlan());
        schedule.setArrivalReal(entity.getArrivalReal());
        schedule.setDeparturePlan(entity.getDeparturePlan());
        schedule.setDepartureReal(entity.getDepartureReal());
        schedule.setPlattformPlan(entity.getPlattformPlan());
        schedule.setPlattformReal(entity.getPlattformReal());
        dto.setSchedule(schedule);

        // Map Annotations
        if (entity.getAnnotations() != null) {
            List<AnnotationReturnDto> annList = entity.getAnnotations().stream()
                .map(annEntity -> {
                    /*AnnotationReturnDto annDto = new AnnotationReturnDto();
                    annDto.setId(annEntity.getId());
                    annDto.setSource(annEntity.getSource());
                    annDto.setText(annEntity.getText());
                    annDto.setCode(annEntity.getCode());
                    annDto.setMessageTime(annEntity.getMessageTime());
                    return annDto;*/
                    return AnnotationReturnDto.of(annEntity);
                })
                .collect(Collectors.toList());
            dto.setAnnotations(annList);
        }

        return dto;
    }


    /**
     * Static factory method to create a DTO from a Database Entity
     */
    public static GetTripDto of(TripStopBasicEntity entity) {
        if (entity == null) return null;

        GetTripDto dto = new GetTripDto();
        
        // Root fields: Combine ID components as requested
        dto.setId(entity.getId());
        dto.setStationName(entity.getStationName());

        // Map Train Info
        TrainInfo info = new TrainInfo();
        info.setTripId(entity.getTripId());
        if (entity.getLine() != null && !entity.getLine().isBlank()) {
            info.setLine(entity.getLine());
        } /*else if (entity.getTrainCategory() != null && !entity.getTrainCategory().isBlank()) {
            info.setLine(entity.getTrainCategory().concat(String.valueOf(entity.getTrainNumber())));
        }*/
        dto.setTrainInfo(info);

        // Map Schedule
        Schedule schedule = new Schedule();
        schedule.setArrivalPlan(entity.getArrivalPlan());
        schedule.setArrivalReal(entity.getArrivalReal());
        schedule.setDeparturePlan(entity.getDeparturePlan());
        schedule.setDepartureReal(entity.getDepartureReal());
        schedule.setPlattformPlan(entity.getPlattformPlan());
        schedule.setPlattformReal(entity.getPlattformReal());
        dto.setSchedule(schedule);

        // Map Annotations
        if (entity.getAnnotations() != null) {
            List<AnnotationReturnDto> annList = entity.getAnnotations().stream()
                .map(annEntity -> {
                    /*AnnotationReturnDto annDto = new AnnotationReturnDto();
                    annDto.setId(annEntity.getId());
                    annDto.setSource(annEntity.getSource());
                    annDto.setText(annEntity.getText());
                    annDto.setCode(annEntity.getCode());
                    annDto.setMessageTime(annEntity.getMessageTime());
                    return annDto;*/
                    return AnnotationReturnDto.of(annEntity);
                })
                .collect(Collectors.toList());
            dto.setAnnotations(annList);
        }

        return dto;
    }
}
