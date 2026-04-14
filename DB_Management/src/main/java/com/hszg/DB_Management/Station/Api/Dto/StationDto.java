package com.hszg.DB_Management.Station.Api.Dto;

import org.springframework.data.domain.Page;

import com.hszg.DB_Management.Station.Database.StationEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Schema(description = "Dto used to verify request body to create station only one of the parameters is needed/required.")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StationDto {

	private Integer stationEva;
	private String stationName;
	
	public StationEntity toDbStation() {
		return new StationEntity(this.stationEva, this.stationName);
	}

    public static Page<StationDto> of(Page<StationEntity> stations) {
		return stations.map(station -> new StationDto(station.getEva(), station.getName()));
	}

	public static StationDto of(StationEntity foundStation) {
		return new StationDto(foundStation.getEva(), foundStation.getName());
	}

}
