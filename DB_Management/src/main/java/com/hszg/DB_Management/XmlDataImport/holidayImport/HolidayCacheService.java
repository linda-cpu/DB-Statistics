package com.hszg.DB_Management.XmlDataImport.holidayImport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hszg.DB_Management.AiData.API.BaseAiDataDto;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
public class HolidayCacheService {

    private final MongoHolidayRepository mongoRepository; 
    private final WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public HolidayCacheService(MongoHolidayRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
        this.webClient = WebClient.builder().baseUrl("https://openholidaysapi.org").build();
    }

    public CachedDayEntity getHolidayInfo(LocalDate date, String subCode) {
        String cacheId = date.toString() + "_" + subCode;
        
        // 1. look for in in the MongoDB Cache 
        Optional<CachedDayEntity> cached = mongoRepository.findById(cacheId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 2. Cache-Miss: get data from OpenHolidays API via WebClient
        Boolean isHoliday = callApi("/PublicHolidays", date, subCode);
        Boolean isSchoolHoliday = callApi("/SchoolHolidays", date, subCode);

        // Don't cache if API was unreachable — let it retry next time
        if (isHoliday == null || isSchoolHoliday == null) {
            log.warn("Skipping cache for {}/{} due to API failure", date, subCode);
            CachedDayEntity fallback = new CachedDayEntity();
            fallback.setId(cacheId);
            fallback.setDate(date);
            fallback.setSubdivisionCode(subCode);
            fallback.setPublicHoliday(null);
            fallback.setSchoolHoliday(null);
            return fallback; // return without saving
        }

        // 3. save in MongoDB for the next time
        CachedDayEntity newCacheEntry = new CachedDayEntity();
        newCacheEntry.setId(cacheId);
        newCacheEntry.setDate(date);
        newCacheEntry.setSubdivisionCode(subCode);
        newCacheEntry.setPublicHoliday(isHoliday);
        newCacheEntry.setSchoolHoliday(isSchoolHoliday);

        return mongoRepository.save(newCacheEntry);
    }

    private Boolean callApi(String endpoint, LocalDate date, String subCode) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(endpoint)
                            .queryParam("countryIsoCode", "DE")
                            .queryParam("subdivisionCode", subCode)
                            .queryParam("validFrom", date.toString())
                            .queryParam("validTo", date.toString())
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // .block() ist im sequentiellen XML-Import völlig okay

            // if the array is not empty, there is a public holiday/school holiday
            if (response == null){
                throw new RuntimeException("Holiday API returned null response for " + endpoint);
            }; 

            JsonNode responseNode = objectMapper.readTree(response);
            return responseNode != null && responseNode.isArray() && !responseNode.isEmpty();
        
        } catch (Exception e) {
            log.error("Holiday API call failed for {} on {}/{}: {}", endpoint, date, subCode, e.getMessage());
            return null;
        }
    }

    public void populateHolidayData(BaseAiDataDto dto, LocalDate localDate, String rawState) {
        if (rawState == null || rawState.isBlank()) {
            log.warn("No federal state provided for station {}. Holiday flags will remain false.", dto.getStationEva());
            dto.setPublicHoliday(false);
            dto.setSchoolHoliday(false);
            return;
        }

        // Ensure a consistent “DE-XX” format
        String subCode = rawState.startsWith("DE-") ? rawState : "DE-" + rawState;

        // call the existing cache/API logic
        CachedDayEntity holidayInfo = this.getHolidayInfo(localDate, subCode);

        // absolutely null-safe transfer to the primitive boolean fields of the DTO
        dto.setPublicHoliday(Boolean.TRUE.equals(holidayInfo.getPublicHoliday()));
        dto.setSchoolHoliday(Boolean.TRUE.equals(holidayInfo.getSchoolHoliday()));
    }
}
