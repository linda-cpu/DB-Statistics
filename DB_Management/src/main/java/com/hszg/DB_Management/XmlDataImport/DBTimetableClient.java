package com.hszg.DB_Management.XmlDataImport;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.hszg.DB_Management.XmlDataImport.modelChangeData.Timetable;
import com.hszg.DB_Management.XmlDataImport.modelStationInfo.StationInfo;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * DB timetable client to fetch timetable data from DB (Deutsche Bahn) API
 * 
 */
@Service
@Slf4j
public class DbTimetableClient {

	private final WebClient webClient;

	public DbTimetableClient(WebClient webClient) {
		this.webClient = webClient;
	}
	
	public Timetable fetchTimetablePlan(String evaId, String date, String hour) {
		try {
			return webClient.get()
				.uri("/plan/{evaId}/{date}/{hour}", evaId, date, hour)
				.retrieve()
				.onStatus(HttpStatusCode::isError, response -> {
                    log.error("API Error: {} for EVA {}", response.statusCode(), evaId);
                    return Mono.error(new ResponseStatusException(response.statusCode(), "External API Error"));
                })
				.bodyToMono(Timetable.class)
				.block();
		} catch (Exception e) {
			log.error("Failed to connect to DB API to fetch plan data for EVA {}: {}", evaId, e.getMessage());
			return null; 	
		}
	}
	
	public Timetable fetchTimetableChange(String evaId) {
		try {
			return webClient.get()
					.uri("/fchg/{evaId}", evaId)
					.retrieve()
					.onStatus(HttpStatusCode::isError, response -> {
						log.error("API Error: {} for EVA {}", response.statusCode(), evaId);
						return Mono.error(new ResponseStatusException(response.statusCode(), "External API Error"));
					})
					.bodyToMono(Timetable.class)
					.block();
		} catch (Exception e) {
			log.error("Failed to connect to DB API to fetch change data for EVA {}: {}", evaId, e.getMessage());
			return null; 	
		}
	}

	/**
	 * Fetch station info by pattern
	 * @param pattern can be a station name or part of it or EVA ID
	 * @return
	 */
	public StationInfo fetchStationInfo(String pattern) {
		try{
			return webClient.get()
					.uri("/station/{pattern}", pattern)
					.retrieve()
					.onStatus(HttpStatusCode::isError, response -> {
						log.error("API Error: {} for pattern (eva/name) {}", response.statusCode(), pattern);
						return Mono.error(new ResponseStatusException(response.statusCode(), "External API Error"));
					})
					.bodyToMono(StationInfo.class)
					.block();
		} catch (Exception e) {
			log.error("Failed to connect to DB API to station informations for pattern {}: {}", pattern, e.getMessage());
			return null; 	
		}
	}
	
}
