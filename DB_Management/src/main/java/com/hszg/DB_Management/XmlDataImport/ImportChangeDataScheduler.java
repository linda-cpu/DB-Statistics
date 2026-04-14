package com.hszg.DB_Management.XmlDataImport;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hszg.DB_Management.Station.Api.IStationRepository;
import com.hszg.DB_Management.Station.Database.StationEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ImportChangeDataScheduler {

	private final TimetableService timetableService;
	private final IStationRepository stationRepository;

    public ImportChangeDataScheduler(TimetableService timetableService, IStationRepository stationRepository) {
        this.timetableService = timetableService;
		this.stationRepository = stationRepository;
    }

    // Cron for 07:00 and 19:00 every day
    @Scheduled(cron = "0 0 7,19 * * *")
    public void scheduledImport() {
        log.info("Starting scheduled XML import...");
		List<StationEntity> stations;

        try {
            stations = stationRepository.findAll();
        } catch (Exception e) {
            log.error("Database unavailable during scheduled import: {}", e.getMessage());
            return;
        }
        
        
        if (stations.isEmpty()) {
            log.info("No stations found in the database. Skipping change data import from DB-API.");
            return;
        }

		log.info("Found {} stations. Starting individual imports.", stations.size());
        for (StationEntity station : stations) {
            try {
                String evaId = String.valueOf(station.getEva());
                log.info("Importing change data for station: {} (EVA: {})", station.getName(), evaId);
                
                try {
                    timetableService.downloadAndImportChangeData(evaId);
                } catch (Exception e) {
                    log.error("Critical failure for station {}: {}", evaId, e.getMessage());
                }
                
            } catch (Exception e) {
                log.error("Failed to import change data for station {}: {}", station.getEva(), e.getMessage());
            }
        }

        log.info("Scheduled task finished: All station imports processed.");
    }
}
