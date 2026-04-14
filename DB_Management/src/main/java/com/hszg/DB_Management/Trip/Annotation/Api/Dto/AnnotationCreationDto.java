package com.hszg.DB_Management.Trip.Annotation.Api.Dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hszg.DB_Management.Trip.Annotation.Database.AnnotationEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dto used to verify request body to create annotation.")
@Getter
@Setter
public class AnnotationCreationDto {

	@Schema(description = "Name of the user creating the annotation", example = "user1")
	@JsonProperty("user_name")
	private String userName;

	@Schema(description = "ID of the service creating the annotation", example = "e2acd009-e0be-4fff-a412-504cae94f106")
	@JsonProperty("service_id")
	private String serviceId;
	
	@Schema(description = "Text content of the annotation", example = "This is an test annotation.")
	private String text;
	
	public AnnotationEntity toDbAnnotation() {
		AnnotationEntity entity = new AnnotationEntity();
		entity.setCode(98); //identifies that it is an annotation from the user
		entity.setMessageTime(Instant.now());
		entity.setSource(this.serviceId.concat("/").concat(this.userName)); // consists of the service name and a user identification to make it unique
		entity.setText(this.text);

		return entity;
	}
}
