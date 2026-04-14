package com.hszg.DB_Management.Trip.Annotation.Api.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hszg.DB_Management.Trip.Annotation.Database.AnnotationEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnotationReturnDto {

    private String id;
	private String source;
	private String text;
    private int code;
    
    @JsonProperty("changed_datetime")
    private String messageTime;

    public static AnnotationReturnDto of(AnnotationEntity entity) {
        AnnotationReturnDto dto = new AnnotationReturnDto();
        dto.setId(entity.getId());
        dto.setSource(entity.getSource());
        dto.setText(entity.getText());
        dto.setCode(entity.getCode());
        dto.setMessageTime(entity.getMessageTime().toString());
        return dto;
    }
}
