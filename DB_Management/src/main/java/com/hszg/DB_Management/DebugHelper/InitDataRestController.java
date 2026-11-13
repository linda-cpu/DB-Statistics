package com.hszg.DB_Management.DebugHelper;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hszg.DB_Management.Station.Api.IStationRepository;
import com.hszg.DB_Management.Station.Database.StationEntity;
import com.hszg.DB_Management.Trip.Api.ITripRepository;
import com.hszg.DB_Management.Trip.Database.TripStopEntity;
import com.hszg.DB_Management.WeatherDataImport.WeatherService;
import com.hszg.DB_Management.XmlDataImport.TimetableService;
import com.hszg.DB_Management.XmlDataImport.holidayImport.CachedDayEntity;
import com.hszg.DB_Management.XmlDataImport.holidayImport.HolidayCacheService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;




@RestController
public class InitDataRestController {
	
	private TimetableService timetableService;	
	private HolidayCacheService holidayCacheService;
	private IStationRepository stationRepository;
	private WeatherService weatherService;
	private ITripRepository tripRepository;
	
	public InitDataRestController(TimetableService timetableService, HolidayCacheService holidayCacheService, IStationRepository stationRepository, WeatherService weatherService, ITripRepository tripRepository) {
		this.timetableService = timetableService;
		this.holidayCacheService = holidayCacheService;
		this.stationRepository = stationRepository;
		this.weatherService = weatherService;
		this.tripRepository = tripRepository;
	}
	
	@Operation(summary = "Trigger local XML import", description = "Manually starts the process of importing XML files from the configured local directory")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Import process triggered successfully",
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
			content = {@Content(mediaType = "plain/text")}
		)
	})
	@PostMapping("/import/xmls")
	public void importXmls() throws IOException {
		timetableService.importLocalXmls();
	}

	@GetMapping("/holiday")
	public String searchForHoliday(
		@Parameter(description = "Date to check for holiday information (format: YYYY-MM-DD)", example = "2026-05-07", required = true)
		@RequestParam LocalDate date) {
		CachedDayEntity cachedDay = holidayCacheService.getHolidayInfo(date, "DE-SN");

		return cachedDay.toString();
	}

	@PostMapping("/holiday")
	public void fillTripsWithHoliday() {
		List<TripStopEntity> trips = tripRepository.findAll();

		List<TripStopEntity> updatedTrips = trips.stream()
				.filter(stopDB -> stopDB.getSchoolHoliday() == null || stopDB.getPublicHoliday() == null)
				.peek(stopDB -> {
				Instant targetDateTime = stopDB.getDeparturePlan() != null 
						? stopDB.getDeparturePlan() 
						: stopDB.getArrivalPlan();

				if (targetDateTime != null) {
					timetableService.handleHolidayInfo(targetDateTime, stopDB);
				}
			})
			.toList();

		if (!updatedTrips.isEmpty()) {
			tripRepository.saveAll(updatedTrips);
		}
	}

	@Operation(summary = "init historical weather data (in empty database!)")
	@PostMapping("/weather")
	public void initHistoricalWeatherDB() {
		
		LocalDate startDate = LocalDate.of(2025, 12, 18);
		//LocalDate startDate = LocalDate.of(2026, 06, 9);
		LocalDate endDate = LocalDate.now();
		
		// list all stations
		List<StationEntity> stations = stationRepository.findAll();
		weatherService.fetchHistoricalDataInit(stations, startDate.toString(), endDate.toString());

	}
	
}
