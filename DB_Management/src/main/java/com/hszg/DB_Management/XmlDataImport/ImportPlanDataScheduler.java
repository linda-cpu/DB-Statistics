package com.hszg.DB_Management.XmlDataImport;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hszg.DB_Management.Station.Api.IStationRepository;
import com.hszg.DB_Management.Station.Database.StationEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ImportPlanDataScheduler {

	private final TimetableService timetableService;
	private final IStationRepository stationRepository;

    public ImportPlanDataScheduler(TimetableService timetableService, IStationRepository stationRepository) {
        this.timetableService = timetableService;
		this.stationRepository = stationRepository;
    }

    // Cron for 12:00 every day
    @Scheduled(cron = "0 0 12 * * *")
    public void scheduledImport() {
        log.info("Starting scheduled plan data XML import...");
		List<StationEntity> stations;
		try {
			stations = stationRepository.findAll();
		} catch (Exception e) {
			log.error("Database unavailable during scheduled import: {}", e.getMessage());
			return; 
		}
		
        
        if (stations.isEmpty()) {
            log.info("No stations found in the database. Skipping plan data import from DB-API.");
            return;
        }

		// prepare date string
		String dateStr = DateTimeFormatter.ofPattern("yyMMdd")
			.withZone(ZoneOffset.UTC)
			.format(Instant.now());

		log.info("Found {} stations. Starting individual imports.", stations.size());

        for (StationEntity station : stations) {
			String evaId = String.valueOf(station.getEva());

			// loop through every hour of the day
			for (int hour = 0; hour < 24; hour++) {
				try {
					String hourStr = String.format("%02d", hour);					
					timetableService.downloadAndImportPlanData(evaId, dateStr, hourStr);
				} catch (Exception e) {
					log.error("Critical failure for station {} at hour {}: {}", evaId, hour, e.getMessage());
				}
			}
		}
        log.info("Scheduled task finished: All station imports processed.");

    }

}
