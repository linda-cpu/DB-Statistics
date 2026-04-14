package com.hszg.DB_Management.TrainLine.Api;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hszg.DB_Management.TrainLine.Database.LinesPerStationEntity;

@Repository
public interface ITrainLineRepository extends MongoRepository<LinesPerStationEntity, Integer> {
    
}
