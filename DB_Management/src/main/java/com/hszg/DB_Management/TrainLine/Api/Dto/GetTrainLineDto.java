package com.hszg.DB_Management.TrainLine.Api.Dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hszg.DB_Management.TrainLine.Database.LinesPerStationEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Dto used to verify request body to create station only one of the parameters is needed/required.")
@Getter
@Setter
public class GetTrainLineDto {

    @JsonProperty("station_eva")
	private int eva;
	
	private List<String> lines = new ArrayList<>();



    public static GetTrainLineDto of(LinesPerStationEntity entity) {

        if (entity == null || entity.getLines().isEmpty()) return null;

        GetTrainLineDto dto = new GetTrainLineDto();
        dto.setEva(entity.getEva());
        dto.setLines(entity.getLines());
        return dto;

    }
}
