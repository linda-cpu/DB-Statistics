package com.hszg.DB_Management.WeatherDataImport;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hszg.DB_Management.AiData.API.BaseAiDataDto;
import com.hszg.DB_Management.Station.Database.StationEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WeatherService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void aggregateForecastData(BaseAiDataDto dto, Instant departureTime, int aggHours) {
        ZoneId tz = ZoneId.of("Europe/Berlin");
        ZonedDateTime departureZoned = departureTime.atZone(tz);

        // 2. load station for coordinates
        StationEntity station = mongoTemplate.findById(dto.getStationEva(), StationEntity.class);
        if (station == null) {
            log.error("Station with EVA {} not found in DB.", dto.getStationEva());
            return;
        }

        // 3. calculate  time frame (start_date and end_date for the API)
        ZonedDateTime startZoned = departureZoned.minusHours(aggHours);
        LocalDate startDate = startZoned.toLocalDate();
        LocalDate endDate = departureZoned.toLocalDate();

        String url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + station.getLatitude() +
                "&longitude=" + station.getLongitude() +
                "&start_date=" + startDate.toString() +
                "&end_date=" + endDate.toString() +
                "&hourly=temperature_2m,relative_humidity_2m,rain,snowfall,wind_gusts_10m,wind_speed_10m,shortwave_radiation" +
                "&timezone=Europe%2FBerlin";

        try {
            // direectly execute HTTP Request
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Weather API Error ({}): {}", response.statusCode(), response.body());
                return;
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            // if an array is returned (Multi-Station-Structure), take the first element, otherwise the root object itself
            JsonNode stationNode = rootNode.isArray() ? rootNode.get(0) : rootNode;
            
            JsonNode hourly = stationNode.path("hourly");
            JsonNode timeArray = hourly.path("time");
            if (timeArray.isMissingNode() || timeArray.isEmpty()) {
                log.warn("No hourly weather data found in the API response.");
                return;
            }

            // 4. read Arrays
            JsonNode tempArray = hourly.path("temperature_2m");
            JsonNode humidityArray = hourly.path("relative_humidity_2m");
            JsonNode rainArray = hourly.path("rain");
            JsonNode snowArray = hourly.path("snowfall");
            JsonNode gustArray = hourly.path("wind_gusts_10m");
            JsonNode speedArray = hourly.path("wind_speed_10m");
            JsonNode radiationArray = hourly.path("shortwave_radiation");

            // Aggregation variablen
            double totalTemp = 0, totalHumidity = 0, totalRain = 0, totalSnow = 0, maxWind = 0, totalWindSpeed = 0, totalRad = 0;
            int countedHours = 0;

            // define time frame for filtering in storage
            Instant endInstant = departureTime;
            Instant startInstant = departureTime.minus(aggHours, ChronoUnit.HOURS);

            // 5. only aggregate hours that fall within the requested aggHours window
            for (int i = 0; i < timeArray.size(); i++) {
                String localTimeStr = timeArray.get(i).asText();
                LocalDateTime localDateTime = LocalDateTime.parse(localTimeStr);
                Instant hourInstant = localDateTime.atZone(tz).toInstant();

                // check, if the hour falls within the target range (gte startInstant and lte endInstant)
                if ((hourInstant.equals(startInstant) || hourInstant.isAfter(startInstant)) && 
                    (hourInstant.equals(endInstant) || hourInstant.isBefore(endInstant))) {
                    
                    totalTemp += tempArray.get(i).asDouble();
                    totalHumidity += humidityArray.get(i).asInt();
                    totalRain += rainArray.get(i).asDouble();
                    totalSnow += snowArray.get(i).asDouble();
                    totalWindSpeed += speedArray.get(i).asDouble();
                    totalRad += radiationArray.get(i).asDouble();
                    
                    double currentGust = gustArray.get(i).asDouble();
                    if (currentGust > maxWind) {
                        maxWind = currentGust;
                    }
                    
                    countedHours++;
                }
            }

            if (countedHours == 0) {
                log.warn("No API weather hours fit into the calculation window.");
                return;
            }

            // 6. DTO with the calculated average/sum values
            dto.setAvgTemperature(totalTemp / countedHours);
            dto.setAvgHumidity((totalHumidity / countedHours) / 100.0);
            dto.setTotalRain(totalRain);
            dto.setTotalSnowfall(totalSnow);
            dto.setMaxWindGusts(maxWind);
            dto.setAvgWindSpeed(totalWindSpeed / countedHours);
            dto.setAvgRadiation(totalRad / countedHours);

            log.info("Weather data successfully aggregated directly from API for the last {} hours.", countedHours);

        } catch (Exception e) {
            log.error("Error by On-Demand weather aggregation: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fetches hourly weather data specifically from the Historical Archive API 
     * for a given date (YYYY-MM-DD).
     */
    public void fetchHistoricalDataInit(List<StationEntity> allStations, String startDate, String endDate) {
        if (allStations == null || allStations.isEmpty()) return;

        // Open-Meteo allows a maximum of 50 stations per single URL request
        int batchSize = 50; 
        for (int i = 0; i < allStations.size(); i += batchSize) {
            List<StationEntity> batch = allStations.subList(i, Math.min(i + batchSize, allStations.size()));
            

            // Extract EVAs from this specific batch
            List<Integer> batchEvas = batch.stream().map(StationEntity::getEva).collect(Collectors.toList());
            
            // is only done for updates (not for init)
            // If we are looking up a single day (e.g., automated updates where startDate == endDate)
            if (startDate.equals(endDate) && doRecordsExistForDate(batchEvas, startDate)) {
                log.warn("Skipping batch: Weather data for these stations already exists for " + startDate);
                continue; // Skip calling the API entirely for this batch
            }


            // Join latitudes and longitudes into comma-separated strings
            String latitudes = batch.stream().map(s -> String.valueOf(s.getLatitude())).collect(Collectors.joining(","));
            String longitudes = batch.stream().map(s -> String.valueOf(s.getLongitude())).collect(Collectors.joining(","));

            String url = "https://archive-api.open-meteo.com/v1/archive?" +
                    "latitude=" + latitudes +
                    "&longitude=" + longitudes +
                    "&start_date=" + startDate +
                    "&end_date=" + endDate +
                    "&hourly=temperature_2m,relative_humidity_2m,rain,snowfall,wind_gusts_10m,wind_speed_10m,shortwave_radiation" +
                    "&timezone=Europe%2FBerlin";

            executeApiRequestAndSave(url, batch);

            // Short pause to respect the fair-use rate limits of the free API tier
            try { 
                Thread.sleep(1000); 
            } catch (InterruptedException e) { 
                Thread.currentThread().interrupt(); 
            }
        }
    }

    /**
     * Checks if weather records already exist for a given batch of stations on a specific date.
     * @param stationEvas List of station EVA numbers to check
     * @param targetDate The date to inspect (Format: "YYYY-MM-DD")
     * @return true if ANY records exist for these parameters, false if the day is completely empty
     */
    public boolean doRecordsExistForDate(List<Integer> stationEvas, String targetDate) {
        ZoneId tz = ZoneId.of("Europe/Berlin");
        LocalDate date = LocalDate.parse(targetDate);

        // Construct the start and end of the target day in UTC
        Instant dayStart = date.atStartOfDay(tz).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(tz).minusNanos(1).toInstant();

        org.springframework.data.mongodb.core.query.Query query = 
            new org.springframework.data.mongodb.core.query.Query(
                org.springframework.data.mongodb.core.query.Criteria.where("eva").in(stationEvas)
                    .and("timestamp").gte(dayStart).lte(dayEnd)
            );

        return mongoTemplate.exists(query, WeatherEntity.class);
    }

    /**
     * 3. Fetches historical data for ALL stations on ONE specific day.
     * Perfect for a nightly cron job to pull the historical data of yesterday.
     * @param targetDate Format: "YYYY-MM-DD"
     */
    public void fetchHistoricalDataUpdate(List<StationEntity> allStations, String targetDate) {
        // Since it uses the exact same endpoint structure, we reuse the Init logic with matching start/end dates
        fetchHistoricalDataInit(allStations, targetDate, targetDate);
    }

    /**
     * Centralized execution engine that parses multi-station / multi-day payloads
     * and writes directly to the Time-Series Collection.
     */
    private void executeApiRequestAndSave(String apiUrl, List<StationEntity> stationsInUrlOrder) {
        if (stationsInUrlOrder == null || stationsInUrlOrder.isEmpty()) {
            log.error("Aborted: The station list cannot be null or empty!");
            return;
        }

        // Force time-series collection creation if it was manually dropped or missing
        if (!mongoTemplate.collectionExists("weather")) {
            mongoTemplate.createCollection(WeatherEntity.class);
            log.info("Time-Series Collection 'weather' successfully initialized via Java code metadata attributes.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("HTTP Failure (" + response.statusCode() + "): " + response.body());
                return;
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            List<WeatherEntity> entitiesToInsert = new ArrayList<>();

            // If multiple coordinates are requested, Open-Meteo responds with a JSON array.
            // If only one station is requested, it responds with a single JSON object.
            if (rootNode.isArray()) {
                for (int i = 0; i < rootNode.size(); i++) {
                    JsonNode locationNode = rootNode.get(i);
                    StationEntity currentStation = stationsInUrlOrder.get(i);
                    parseAndAddHourlyRecords(locationNode, currentStation, entitiesToInsert);
                }
            } else {
                // Single station fallback handling (primarily for fetchForecastData)
                StationEntity singleStation = stationsInUrlOrder.get(0);
                parseAndAddHourlyRecords(rootNode, singleStation, entitiesToInsert);
            }

            if (!entitiesToInsert.isEmpty()) {
                mongoTemplate.insertAll(entitiesToInsert);
                log.info("Successfully inserted " + entitiesToInsert.size() + " total hourly weather records into MongoDB.");
            }

        } catch (Exception e) {
            log.error("Error processing weather API request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parses the hourly dataset node of a specific location and maps it to the entity buffer list.
     */
    private void parseAndAddHourlyRecords(JsonNode stationNode, StationEntity station, List<WeatherEntity> outputList) {
        JsonNode hourly = stationNode.path("hourly");
        JsonNode timeArray = hourly.path("time");
        if (timeArray.isMissingNode()) return;

        JsonNode tempArray = hourly.path("temperature_2m");
        JsonNode humidityArray = hourly.path("relative_humidity_2m");
        JsonNode rainArray = hourly.path("rain");
        JsonNode snowArray = hourly.path("snowfall");
        JsonNode gustArray = hourly.path("wind_gusts_10m");
        JsonNode speedArray = hourly.path("wind_speed_10m");
        JsonNode radiationArray = hourly.path("shortwave_radiation");

        int evaStr = station.getEva();

        // Iterate through all hours in the response array
        for (int i = 0; i < timeArray.size(); i++) {
            WeatherEntity entity = new WeatherEntity();
            
            // Format incoming timestamp string (e.g. "2026-06-11T00:00") to standard Instant
            String localTime = timeArray.get(i).asText();
            LocalDateTime localDateTime = LocalDateTime.parse(localTime);
            Instant instant = localDateTime.atZone(ZoneId.of("Europe/Berlin")).toInstant();
            entity.setTimestamp(instant);
            entity.setEva(evaStr);
            entity.setTemperature(tempArray.get(i).asDouble());
            entity.setHumidity(humidityArray.get(i).asInt());
            entity.setRain(rainArray.get(i).asDouble());
            entity.setSnowfall(snowArray.get(i).asDouble());
            entity.setWindGusts(gustArray.get(i).asDouble());
            entity.setWindSpeed(speedArray.get(i).asDouble());
            entity.setRadiation(radiationArray.get(i).asDouble());
            
            outputList.add(entity);
        }
    }

}