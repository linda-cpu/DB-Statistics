package com.hszg.db_statistics.service;

import com.hszg.db_statistics.client.service.DbManagementApiClient;
import com.hszg.db_statistics.client.dto.DelayReasonDto;
import com.hszg.db_statistics.client.dto.annotations.AnnotationDto;
import com.hszg.db_statistics.client.dto.trips.TripPageResponse;
import com.hszg.db_statistics.client.dto.trips.TripDto;
import com.hszg.db_statistics.client.dto.trips.TripScheduleDto;
import com.hszg.db_statistics.enums.MetricType;
import com.hszg.db_statistics.enums.TimeInterval;
import com.hszg.db_statistics.dto.StatisticDto;
import com.hszg.db_statistics.util.DurationHelper;
import com.hszg.db_statistics.util.StatisticUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticService {

    private final DbManagementApiClient dbManagementApiClient;
    private final WebClient WebClient;

    public List<StatisticDto> getDelaySeries(int stationEva, String line, Instant from, Instant to,
                                             MetricType metricType, TimeInterval interval) {
        log.info("Generating delay series chart. Station: {}, Interval: {}", stationEva, interval);
        
        Map<String, int[]> intervalData = new HashMap<>();

        processTripsInBatches(stationEva, line, from, to, batch -> {
            for (TripDto trip : batch) {
                TripScheduleDto s = trip.getSchedule();
                if (s == null && s.getArrivalPlan() == null) continue;

                long delay = DurationHelper.calculateDelayInMinutes(s.getArrivalPlan(), s.getArrivalReal());
                if (delay == -9999) continue;

                String key = StatisticUtils.generateIntervalKey(s.getArrivalPlan(), interval);
                int[] stats = intervalData.computeIfAbsent(key, k -> new int[2]);

                //Index 0: Total Count
                stats[0]++;

                //Index 1: Delayed Count
                if (delay >= 6) {
                    stats[1]++;
                }


            }
        });
        System.out.println(intervalData);

        List<StatisticDto> series = new ArrayList<>();
        intervalData.keySet().stream().sorted().forEach(key -> {
            int[] stats = intervalData.get(key);
            double value;

            if (metricType == MetricType.COUNT) {
                value = stats[1];
            } else {
                value = stats[0] > 0 ? (double) stats[1] / stats[0] * 100.0 : 0.0;
                value = Math.round(value * 100.0) / 100.0;
            }
            series.add(new StatisticDto(key, value));
        });

        return series;
    }

    public List<StatisticDto> getTopDelayedLines(int stationEva, Instant from, Instant to, int limit) {
        log.info("Calculating top delayed lines. Station: {}", stationEva);
        
        Map<String, Integer> delayCounts = new HashMap<>();

        processTripsInBatches(stationEva, null, from, to, batch -> {
            for (TripDto trip : batch) {
                if (StatisticUtils.isDelayed(trip)) {
                    
                    String lineName = trip.getTrainInfo() != null ? trip.getTrainInfo().getLine() : null;
                    if (lineName != null && !lineName.isBlank()) {
                        delayCounts.merge(lineName, 1, Integer::sum);
                    }
                }
            }
        });

        return delayCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(entry -> new StatisticDto(entry.getKey(), (double) entry.getValue()))
                .toList();
    }

    public List<StatisticDto> getTopDelayReasons(int stationEva, Instant from, Instant to, int limit) {
        log.info("Analyzing top delay reasons. Station: {}", stationEva);
        
        Map<Integer, Integer> codeCounts = new HashMap<>();
        Map<Integer, String> codeTexts = new HashMap<>();
        Map<Integer, String> localReasonCache = new HashMap<>();

        processTripsInBatches(stationEva, null, from, to, batch -> {
            for (TripDto trip : batch) {
                if (trip.getAnnotations() == null) continue;

                for (AnnotationDto annotation : trip.getAnnotations()) {
                    int code = annotation.getCode();
                    if (code == 98 || code == 0) continue;

                    try {
                        String reason = codeTexts.computeIfAbsent(code, c -> {
                            DelayReasonDto dto = dbManagementApiClient.getDelayReason(c);
                            return (dto != null) ? dto.getReason() : null;
                        });

                        if (reason != null) {
                            codeCounts.merge(code, 1, Integer::sum);
                        }
                    } catch (Exception e) {
                        log.warn("Code {} skipped: Not in DB or Error.", code);
                    }
                }
            }
        });

        return codeCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(entry -> new StatisticDto(
                        String.valueOf(entry.getKey()), 
                        (double) entry.getValue(), 
                        codeTexts.get(entry.getKey())))
                .toList();
    }

    private void processTripsInBatches(int stationEva, String line, Instant from, Instant to,
                                       Consumer<List<TripDto>> batchProcessor) {
        int page = 0;
        int pageSize = 100;
        int totalPages = 0;
        
        log.debug("Starting batch fetch. Station: {}, PageSize: {}", stationEva, pageSize);

        do {
            try {
                TripPageResponse response = dbManagementApiClient.getTrips(stationEva, line, from, to, page, pageSize);

                if (response == null || response.getContent() == null || response.getContent().isEmpty()) {
                    break;
                }

                batchProcessor.accept(response.getContent());

                if (response.getTotalPages() > 0) {
                    totalPages = response.getTotalPages();
                } else {
                    if (response.getContent().size() < pageSize) break;
                    totalPages = page + 2;
                }
                page++;
                
            } catch (Exception e) {
                log.error("Error fetching batch page {}: {}", page, e.getMessage());
                break;
            }
        } while (page < totalPages);
        
        log.debug("Batch processing finished. Total pages fetched: {}", page);
    }
}