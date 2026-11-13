package com.hszg.DB_Management.WeatherDataImport;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hszg.DB_Management.Station.Api.IStationRepository;
import com.hszg.DB_Management.Station.Database.StationEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ImportWeatherDataScheduler {
   
    private WeatherService weatherService;
    private final IStationRepository stationRepository;

    public ImportWeatherDataScheduler(WeatherService weatherService, IStationRepository stationRepository) {
        this.weatherService = weatherService;
        this.stationRepository = stationRepository;
    }


    /**
     * Cron schedule: Runs every day at 1:00 AM.
     * "0 0 1 * * ?" -> Second, Minute, Hour, Day of Month, Month, Day of Week.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void importYesterdayWeatherData() {
        // 1. Calculate yesterday's date string (Format: YYYY-MM-DD)
        String yesterdayStr = LocalDate.now().minusDays(1).toString();
        log.info("Starting automated daily weather import for date: " + yesterdayStr);

        // 2. Fetch all active stations registered in your system
        List<StationEntity> allStations = stationRepository.findAll();

        if (allStations.isEmpty()) {
            log.warn("No stations found in the database. Aborting daily weather update.");
            return;
        }

        // 3. Trigger the update 
        weatherService.fetchHistoricalDataUpdate(allStations, yesterdayStr);
        
        log.info("Automated weather synchronization completed for " + yesterdayStr);
    }
}
