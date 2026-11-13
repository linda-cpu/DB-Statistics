package com.hszg.DB_Management.Strike.Database;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hszg.DB_Management.TrainLine.Api.Dto.GetTrainLineDto;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "strikes")
public class StrikeEntity {
    @Id
    private String id;

    @NonNull
    private List<GetTrainLineDto> affectedLines; // Store affected lines as a comma-separated string

    private String description;

    private Instant startTime;

    private Instant endTime;

}
