package com.hszg.DB_Management.Trip.Api;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import com.hszg.DB_Management.Trip.Database.TripStopEntity;

public class ITripRepositoryImpl implements ITripRepositoryCustom {
	
	private final MongoTemplate mongoTemplate;

    public ITripRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
	public Page<TripStopEntity> findTrips(int stationEva, Instant from, Instant to, String line, Pageable pageable) {
		Query query = new Query();
		
		query.addCriteria(Criteria.where("stationEva").is(stationEva));
		
		if (from != null && to != null) {
            query.addCriteria(Criteria.where("arrivalPlan").gte(from).lte(to));
        } else if (from != null) {
            query.addCriteria(Criteria.where("arrivalPlan").gte(from));
        } else if (to != null) {
            query.addCriteria(Criteria.where("arrivalPlan").lte(to));
        }
		
		if (line != null && !line.isEmpty()) {
            query.addCriteria(Criteria.where("line").is(line));
        }
		
        query.with(pageable);
		
		//return mongoTemplate.find(query, TripStopEntity.class);

        //query.fields().include("stationEva", "line", "arrival_plan", "tripId");
        List<TripStopEntity> trips = mongoTemplate.find(query, TripStopEntity.class);

        // 3. Use PageableExecutionUtils to return a Page
        // This executes a count query only when the page isn't full/last
        return PageableExecutionUtils.getPage(
            trips,
            pageable,
            () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), TripStopEntity.class)
        );
	}

}
