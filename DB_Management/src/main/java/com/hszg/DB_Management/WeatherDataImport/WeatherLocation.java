package com.hszg.DB_Management.WeatherDataImport;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherLocation {
    
    private double latitude;
    private double longitude;

    public WeatherLocation(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }
}
