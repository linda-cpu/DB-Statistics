package com.hszg.DB_Management.AiData.API;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Timer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hszg.DB_Management.Station.Api.IStationRepository;
import com.hszg.DB_Management.Station.Database.StationEntity;
import com.hszg.DB_Management.Strike.API.IStrikeRepository;
import com.hszg.DB_Management.Strike.Database.StrikeEntity;
import com.hszg.DB_Management.Trip.Api.ITripRepository;
import com.hszg.DB_Management.Trip.Database.TripStopEntity;
import com.hszg.DB_Management.WeatherDataImport.WeatherService;
import com.hszg.DB_Management.XmlDataImport.GermanStateFinder;
import com.hszg.DB_Management.XmlDataImport.holidayImport.CachedDayEntity;
import com.hszg.DB_Management.XmlDataImport.holidayImport.HolidayCacheService;
import com.hszg.DB_Management.XmlDataImport.modelStationInfo.Station;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "AI Data", description = "Endpoints for retrieving data for AI training.")
public class AiDataRestController {

    private final HolidayCacheService holidayCacheService;
    private final AiDataAggregationService aggregationService;
    private final WeatherService weatherService;
    private final IStationRepository stationRepository;
    private final GermanStateFinder germanStateFinder;
    private final IStrikeRepository strikeRepository;

    public AiDataRestController(HolidayCacheService holidayCacheService, AiDataAggregationService aggregationService, WeatherService weatherService, IStationRepository stationRepository, GermanStateFinder germanStateFinder, IStrikeRepository strikeRepository) {
        this.holidayCacheService = holidayCacheService;
        this.aggregationService = aggregationService;
        this.weatherService = weatherService;
        this.stationRepository = stationRepository;
        this.germanStateFinder = germanStateFinder;
        this.strikeRepository = strikeRepository;
    }

    @Operation(summary = "Get AI training data", description = "Returns a list of trips with delay information for AI training purposes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the trips", 
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ReturnAiDataDto.class))}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing", 
			content = {@Content(mediaType = "plain/text")}
		)
    })
    @GetMapping("/aiData")
    public Page<ReturnAiDataDto> getAiData(
        @Parameter(
            description = "Optional ISO-8601 timeline anchor point to look forward from (UTC format).", 
            example = "2026-06-03T10:00:00Z",
            required = false,
            schema = @Schema(implementation = Instant.class)
        )
        @RequestParam(required = false, value = "from") Instant from, 
        @PageableDefault(size = 20) Pageable pageable,
        @RequestParam(required = false, value = "weatherHours", defaultValue = "5") int weatherHours) {

        log.info("Received ai data request");

        Instant startTimer = Instant.now();

        Page<ReturnAiDataDto> result = aggregationService.getAiDataWithWeather(from, pageable, weatherHours);

        Instant endTimer = Instant.now();
        Duration duration = Duration.between(startTimer, endTimer);
        log.info("Ai data request took " + String.format("%d:%02d.%03d",
            duration.toMinutesPart(),
            duration.toSecondsPart(),
            duration.toMillisPart()));
        
        return result;
    }

    @Operation(
        summary = "Get prediction input data",
        description = "Aggregates all relevant features for a single AI delay prediction: time, holidays, strikes and weather for the given station, line and departure time."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved prediction data",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = BaseAiDataDto.class))}
        ),
        @ApiResponse(responseCode = "400", description = "Invalid parameters (e.g. departure time more than 16 days in the future)",
            content = {@Content(mediaType = "plain/text")}
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
            content = {@Content(mediaType = "plain/text")}
        )
    })
    @GetMapping("/predictionData")
    public BaseAiDataDto getPredictionData(
        @Parameter(description = "Planned departure time in UTC (ISO-8601)", example = "2026-06-20T14:30:00Z", required = true)
        @RequestParam Instant dateTime,

        @Parameter(description = "7-digit EVA station number", example = "8010131", required = true)
        @RequestParam int stationEva,

        @Parameter(description = "Train line identifier", example = "S1", required = true)
        @RequestParam String line,

        @Parameter(description = "Number of hours before departure to aggregate weather data over", example = "5", required = true)
        @RequestParam int aggHours
    ) {

        long daysInFuture = Duration.between(Instant.now(), dateTime).toDays();
        if (daysInFuture > 16) {
            throw new IllegalArgumentException("Weather forecast is only available up to 16 days in advance.");
        }

        BaseAiDataDto dto = new BaseAiDataDto();

        dto.setLine(line);
        dto.setStationEva(stationEva);

        ZonedDateTime departureZoned = dateTime.atZone(ZoneId.of("Europe/Berlin"));
        dto.setMinutesOfDay(departureZoned.getHour()*60 + departureZoned.getMinute());
        dto.setDayOfWeek(departureZoned.getDayOfWeek().getValue());

        // Holidays
        Optional<StationEntity> stationOpt = stationRepository.findById(stationEva);
    
        if (stationOpt.isPresent() && stationOpt.get().getLatitude() != null && stationOpt.get().getLongitude() != null) {
            StationEntity station = stationOpt.get();
            String rawState = germanStateFinder.identifyState(station.getLatitude(), station.getLongitude());
            
            holidayCacheService.populateHolidayData(dto, departureZoned.toLocalDate(), rawState);
        } else {
            log.warn("Station {} nicht gefunden oder unvollständig. Ferien-Flags bleiben false.", stationEva);
            dto.setPublicHoliday(false);
            dto.setSchoolHoliday(false);
        }

        // Strike
        boolean isStrike = false;
        List<StrikeEntity> activeStrikes = strikeRepository
            .findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(dateTime, dateTime);

        if (!activeStrikes.isEmpty()) {
            isStrike = activeStrikes.stream()
                .flatMap(strike -> strike.getAffectedLines().stream())
                .anyMatch(l -> l.getLines().contains(line) && l.getEva() == stationEva);
        }
        dto.setStrike(isStrike);
        
        // Weather
        weatherService.aggregateForecastData(dto, dateTime, aggHours);


        return dto;
    }
    
}
