package com.hszg.DB_Management.WeatherDataImport;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.TimeSeries;
import org.springframework.data.mongodb.core.timeseries.Granularity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TimeSeries(collection = "weather", timeField = "timestamp", metaField = "eva", granularity = Granularity.HOURS)
public class WeatherEntity {
    @Id
    private String id;
    private Instant timestamp; 
    private int eva; 
    
    private double temperature;
    private int humidity;
    private double rain;
    private double snowfall;
    private double windGusts;
    private double windSpeed;
    private double radiation;
}
