package com.hszg.DB_Management.Trip.Annotation.UserAnnotation.API.Dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hszg.DB_Management.Trip.Annotation.Api.Dto.AnnotationReturnDto;
import com.hszg.DB_Management.Trip.Annotation.UserAnnotation.Database.UserAnnotationEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAnnotationDto extends AnnotationReturnDto {

    @JsonProperty("stop_id")
    private String stopId;

    public static List<UserAnnotationDto> of(List<UserAnnotationEntity> entities) {
        return entities.stream().map(UserAnnotationDto::of).toList();
    }

    public static UserAnnotationDto of(UserAnnotationEntity entity) {
        UserAnnotationDto dto = new UserAnnotationDto();
        dto.setStopId(entity.getStopId()); // id of the stop
        dto.setId(entity.getId()); // id of the annotation
        dto.setSource(entity.getSource());
        dto.setText(entity.getText());
        dto.setCode(entity.getCode());
        dto.setMessageTime(entity.getMessageTime().toString());
        return dto;
    }
}
