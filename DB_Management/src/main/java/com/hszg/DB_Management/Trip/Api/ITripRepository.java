package com.hszg.DB_Management.Trip.Api;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hszg.DB_Management.Trip.Database.TripStopEntity;

@Repository
public interface ITripRepository extends MongoRepository<TripStopEntity, String>, ITripRepositoryCustom {

	Optional<TripStopEntity> findByTripId(String tripId); // Used to check for existing trips

}
