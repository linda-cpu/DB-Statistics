package com.hszg.DB_Management.Trip.Annotation.Database;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnotationEntity {

	@Id
	private String id = java.util.UUID.randomUUID().toString();

	@NonNull
	private String source;
	
	private String text;
	
	@NonNull
	private int code;
	
	@Field("changed_datetime")
	private Instant messageTime;

	@Override
	public String toString() {
		return "AnnotationEntity [id=" + id + ", source=" + source + ", text=" + text + ", code=" + code
				+ ", messageTime=" + messageTime + "]";
	}

}
