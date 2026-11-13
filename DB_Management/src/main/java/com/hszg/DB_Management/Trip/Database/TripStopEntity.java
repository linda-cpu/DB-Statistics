package com.hszg.DB_Management.Trip.Database;

import java.time.DayOfWeek;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "trips")
@CompoundIndex(name = "station_line_arrival_idx", def = "{'stationEva': 1, 'line': 1, 'arrivalPlan': 1}")
@CompoundIndex(name = "departure_plan_idx", def = "{'departurePlan': 1}")
public class TripStopEntity extends TripStopBasicEntity {

    private DayOfWeek dayOfWeek;
    private Boolean schoolHoliday;
    private Boolean publicHoliday;

}
