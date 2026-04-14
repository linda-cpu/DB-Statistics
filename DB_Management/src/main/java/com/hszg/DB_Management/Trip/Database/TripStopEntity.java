package com.hszg.DB_Management.Trip.Database;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "trips")
@CompoundIndex(name = "station_line_arrival_idx", def = "{'stationEva': 1, 'line': 1, 'arrivalPlan': 1}")
public class TripStopEntity extends TripStopBasicEntity {

}
