package com.hszg.DB_Management.Strike.API;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hszg.DB_Management.Strike.Database.StrikeEntity;
import com.hszg.DB_Management.TrainLine.Api.Dto.GetTrainLineDto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO for creating a new strike")
public class StrikeCreationDto {
    
    @ArraySchema(
        schema = @Schema(implementation = GetTrainLineDto.class),
        arraySchema = @Schema(description = "List of affected stations and their corresponding lines")
    )
    @NotEmpty(message = "At least one affected line must be specified")
    @JsonProperty("affectedLines")
    private List<GetTrainLineDto> affectedLines;



    @Schema(description = "Description of the strike",
            example = "Strike by train drivers affecting multiple lines during rush hour"
    )
    @NotBlank(message = "Description must not be blank")
    @JsonProperty("description")
    private String description;



    @Schema(description = "Start time of the strike",
            example = "2023-10-01T10:00:00Z"
    )
    @NotNull(message = "Start time of the strike must be specified")
    @JsonProperty("startTime")
    private Instant startTime;



    @Schema(description = "End time of the strike",
            example = "2023-10-01T12:00:00Z"
    )
    @NotNull(message = "End time of the strike must be specified")
    @JsonProperty("endTime")
    private Instant endTime;




    // default constructor (required by jackson)
    public StrikeCreationDto() {
    }

    public StrikeEntity toStrikeEntity() {
        StrikeEntity entity = new StrikeEntity();
        entity.setAffectedLines(this.affectedLines);
        entity.setDescription(this.description);
        entity.setStartTime(this.startTime);
        entity.setEndTime(this.endTime);
        return entity;
    }

    // getters and setters
    public List<GetTrainLineDto> getAffectedLines() {
        return affectedLines;
    }

    public void setAffectedLines(List<GetTrainLineDto> affectedLines) {
        this.affectedLines = affectedLines;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
}