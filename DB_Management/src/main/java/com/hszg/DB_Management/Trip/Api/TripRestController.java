package com.hszg.DB_Management.Trip.Api;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hszg.DB_Management.Trip.Api.Dto.GetTripDto;
import com.hszg.DB_Management.Trip.Database.TripStopEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.media.Schema;

@Slf4j
@RestController
@RequestMapping("/stations/{stationEva}/trips")
@Tag(name = "Trips", description = "Endpoints for managing and retrieving train trips for a given station.")
public class TripRestController {
	
	private final ITripRepository repository;
	
	public TripRestController(ITripRepository repository) {
		this.repository = repository;
	}
	

	/**
	 * returns all trips that should have taken place in this time
	 * 
	 * @param stationEva id of station
	 * @param trainCategory RE, ICE, ...
	 * @param trainNumber Number of the train
	 * @param from Instant from when on the trips should be returned
	 * @param to Instant until which time the trips should be returned
	 * @return
	 */
	@Operation(summary = "Get trips for a station", description = "Returns a list of trips filtered by time and train line")
	@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetTripDto.class))}
		),
        @ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
			content = {@Content(mediaType = "plain/text")}
		)
    })
	@GetMapping
	public Page<GetTripDto> getTripsOfStation(@PathVariable int stationEva, 
									@RequestParam(required = false, value = "line") String line,
									@RequestParam(required = false, value = "from") Instant from,
									@RequestParam(required = false, value = "to") Instant to,
									@PageableDefault(size = 20) Pageable pageable) {
		log.info("Received request for trips at station EVA: {}, line: {}, from: {}, to: {}, page: {} of size {}", stationEva, line, from, to, pageable.getPageNumber(), pageable.getPageSize(), pageable.getPageSize());
		
		Page<TripStopEntity> foundTrips = repository.findTrips(stationEva, from, to, line, pageable);
		return foundTrips.map(GetTripDto::of);
	}
	
	/**
	 * returns the trip with this tripId
	 * 
	 * @param stationEva id of station
	 * @param trainCategory RE, ICE, ...
	 * @param trainNumber Number of the train
	 * @param from Instant from when on the trips should be returned
	 * @param to Instant until which time the trips should be returned
	 * @return
	 */
	@Operation(summary = "Get a specific trip by ID (not tripId)", description = "Returns detailed information for a single trip based on its unique ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved the trip", 
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetTripDto.class))}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing", 
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "404", description = "No trip with this ID found in database", 
			content = {@Content(mediaType = "plain/text")}
		)
	})
	@GetMapping("/{id}")
	public GetTripDto getTripsOfStation(@PathVariable int stationEva, @PathVariable String id) {
		log.info("Received request for trip ID: {} at station EVA: {}", id, stationEva);
		
		Optional<TripStopEntity> foundTrip = repository.findById(id);
		
		return foundTrip.map(GetTripDto::of)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No trip with this eva number found in database"));
	}
	
}
