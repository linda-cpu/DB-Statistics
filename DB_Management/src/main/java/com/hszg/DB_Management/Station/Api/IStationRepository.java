package com.hszg.DB_Management.Station.Api;


import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hszg.DB_Management.Station.Database.StationEntity;


@Repository
public interface IStationRepository extends MongoRepository<StationEntity, Integer> {

    Optional<StationEntity> findByName(String stationName);
}
