package com.hszg.DB_Management.Trip.Annotation.Api.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dto used to verify request body to change annotation.")
@Getter
@Setter
public class AnnotationChangeDto {

    @JsonProperty("user_name")
	private String userName;

	@JsonProperty("service_id")
	private String serviceId;

    private String text;

}
