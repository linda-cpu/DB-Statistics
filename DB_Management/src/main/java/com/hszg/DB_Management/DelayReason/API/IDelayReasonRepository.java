package com.hszg.DB_Management.DelayReason.API;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hszg.DB_Management.DelayReason.Database.DelayReasonEntity;

@Repository
public interface IDelayReasonRepository extends MongoRepository<DelayReasonEntity, Integer> {
    
}
