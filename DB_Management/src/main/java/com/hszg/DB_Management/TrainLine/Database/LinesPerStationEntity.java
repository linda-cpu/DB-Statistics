package com.hszg.DB_Management.TrainLine.Database;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "lines")
public class LinesPerStationEntity {

	@Id
	private int eva;
	
	private List<String> lines = new ArrayList<>();

	public void addLine(String line) {
		lines.add(line);
	}
	
}
