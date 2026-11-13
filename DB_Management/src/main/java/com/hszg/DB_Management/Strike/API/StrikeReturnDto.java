package com.hszg.DB_Management.Strike.API;

import com.hszg.DB_Management.Strike.Database.StrikeEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StrikeReturnDto extends StrikeCreationDto {

    public StrikeReturnDto(StrikeEntity strikeEntity) {
        this.setId(strikeEntity.getId());
        this.setAffectedLines(strikeEntity.getAffectedLines());
        this.setDescription(strikeEntity.getDescription());
        this.setStartTime(strikeEntity.getStartTime());
        this.setEndTime(strikeEntity.getEndTime());
    }

    private String id;
}
