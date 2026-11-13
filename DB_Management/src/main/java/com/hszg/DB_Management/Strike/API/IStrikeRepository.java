package com.hszg.DB_Management.Strike.API;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hszg.DB_Management.Strike.Database.StrikeEntity;

@Repository
public interface IStrikeRepository extends MongoRepository<StrikeEntity, String> {

    List<StrikeEntity> findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
        Instant departurePlan, Instant departurePlan2
    );

}