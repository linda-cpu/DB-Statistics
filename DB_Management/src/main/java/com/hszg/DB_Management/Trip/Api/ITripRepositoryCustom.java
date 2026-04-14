package com.hszg.DB_Management.Trip.Api;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hszg.DB_Management.Trip.Database.TripStopEntity;

public interface ITripRepositoryCustom {

	Page<TripStopEntity> findTrips(int stationEva, Instant from, Instant to, String line,
		Pageable pageable);
}
