package com.hszg.DB_Management.Station.Database;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "stations")
public class StationEntity {

	@Id
	private int eva;
	
	@Field("name")
	private String name;

	@Field("latitude")
	private Double latitude;

	@Field("longitude")
	private Double longitude;

	public StationEntity(int eva, String name, Double latitude, Double longitude) {
		this.eva = eva;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
}
