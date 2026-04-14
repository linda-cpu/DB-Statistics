package com.hszg.DB_Management.Station.Api;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hszg.DB_Management.Station.Api.Dto.StationDto;
import com.hszg.DB_Management.Station.Database.StationEntity;
import com.hszg.DB_Management.XmlDataImport.TimetableService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Stations", description = "Endpoints for retrieving and adding stations")
@RestController
@RequestMapping("/stations")
public class StationRestContoller {
	
	IStationRepository repository;
	TimetableService dbApiService;
	
	
	public StationRestContoller(IStationRepository repository, TimetableService dbApiService) {
		this.repository = repository;
		this.dbApiService = dbApiService;
	}

	/**
	 * returns all station with eva number and there names (use pagination ?page=0&size=20)
	 * 
	 * @return 
	 */
	@Operation(summary = "List all stations", description = "Returns a paginated list of all stations stored in the database")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved list",
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StationDto.class))}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
			content = {@Content(mediaType = "plain/text")}
		)
	})
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<StationDto> getStations(@PageableDefault(size = 20) Pageable pageable) {
		log.info("Received request to list all stations");

		Page<StationEntity> stations = repository.findAll(pageable);
		return StationDto.of(stations);
	}
	
	/**
	 * returns all station with eva number and there names
	 * 
	 * @return 
	 */
	@Operation(summary = "Get station details", description = "Returns name and EVA ID for a specific station")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved station",
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StationDto.class))}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "404", description = "No station with this EVA number found",
			content = {@Content(mediaType = "plain/text")}
		)
	})
	@GetMapping(value = "/{stationEva}", produces = MediaType.APPLICATION_JSON_VALUE)
	public StationDto getStation(@PathVariable int stationEva) {
		log.info("Received request for station with EVA number: {}", stationEva);
		
		StationEntity foundStation = repository.findById(stationEva)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No station with this eva number found in database"));
		
		return StationDto.of(foundStation);
	}

	
	/**
	 * adds a new station if the station does not already exists in the database
	 * 
	 * @param stationEva
	 * @param stationName (can be null)
	 * @return 
	 */
	@Operation(summary = "Add a new station", description = "Creates a new station entry. If data is missing, it attempts to fetch details from the external DB-API")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Station successfully created",
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = StationDto.class))}
		),
		@ApiResponse(responseCode = "400", description = "Invalid input - neither EVA nor Name of station provided",
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "404", description = "Station not found via external API",
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "409", description = "Station already exists in the database",
			content = {@Content(mediaType = "plain/text")}
		)
	})
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public StationDto addStation(@RequestBody StationDto station) {
		log.info("Received request to add station: EVA={}, Name={}", station.getStationEva(), station.getStationName());

		if ((station.getStationName() == null || station.getStationName().isBlank()) && station.getStationEva() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either EVA number or station name must be provide.");

		}

		StationEntity stationToSave;
		
		if (station.getStationName() == null || station.getStationName().isBlank()) {
			// only eva number is filled --> get missing information from DB-API
			stationToSave = dbApiService.fetchStationInfo(String.valueOf(station.getStationEva()));
			if (stationToSave == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Station could not be found via external DB-API.");
			}

		} else if (station.getStationEva() == null) {
			// only station name is filled --> get missing information from DB-API
			stationToSave = dbApiService.fetchStationInfo(station.getStationName());
			if (stationToSave == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Station could not be found via external DB-API.");
			}

		} else {
			// both parameters provided
			stationToSave = dbApiService.fetchStationInfo(String.valueOf(station.getStationEva()));
			if (stationToSave == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Station eva does not exists according to the DB-API.");
			} else if (!stationToSave.getName().equals(station.getStationName())) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wrong station name for the provided eva according to the  DB-API.");
			} else {
				stationToSave = station.toDbStation();
			}
		}

		// check if station already exists
		if (stationExists(stationToSave.getEva())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This station already exists in the database.");
		}

		// save and return entity 
		StationEntity savedStation = repository.save(stationToSave);
		return StationDto.of(savedStation);
	}

	private boolean stationExists(int stationEva) {
		return repository.existsById(stationEva);
	}
} 
