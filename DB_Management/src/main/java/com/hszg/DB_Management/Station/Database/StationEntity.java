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

	public StationEntity(int eva, String name) {
		this.eva = eva;
		this.name = name;
	}
	
}
