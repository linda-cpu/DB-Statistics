package com.hszg.DB_Management.AiData.API;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.hszg.DB_Management.Strike.API.IStrikeRepository;
import com.hszg.DB_Management.Strike.Database.StrikeEntity;
import com.hszg.DB_Management.Trip.Database.TripStopEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AiDataAggregationService {

    private final MongoTemplate mongoTemplate;
    private final IStrikeRepository strikeRepository;

    public AiDataAggregationService(MongoTemplate mongoTemplate, IStrikeRepository strikeRepository) {
        this.mongoTemplate = mongoTemplate;
        this.strikeRepository = strikeRepository;
    }

    public Page<ReturnAiDataDto> getAiDataWithWeather(Instant from, Pageable pageable, int hours) {

        // load all potenziell relevanten Strikes only one time (not for each trip)
        List<StrikeEntity> relevantStrikes = strikeRepository
            .findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                from != null ? from : Instant.EPOCH,
                Instant.now()
            );

        // Match criteria to make sure we only pull records with an actual departure plan
        Criteria matchCriteria = Criteria.where("departurePlan").exists(true);
        if (from != null) {
            matchCriteria = Criteria.where("departurePlan")
                .exists(true)
                .gte(java.util.Date.from(from));
        }
        AggregationOperation matchStage = Aggregation.match(matchCriteria);
        AggregationOperation sortStage = Aggregation.sort(Sort.by(Sort.Direction.ASC, "departurePlan"));
        // short count pipeline to get the total number of matching documents (without pagination)
        List<AggregationOperation> countStages = List.of(matchStage, ctx -> new Document("$count", "total"));

        Document countResult = mongoTemplate.aggregate(
            Aggregation.newAggregation(countStages),
            TripStopEntity.class, Document.class
        ).getUniqueMappedResult();

        long total = countResult != null ? ((Number) countResult.get("total")).longValue() : 0;

        // data pipeline: match -> sort -> skip/limit -> lookup
        List<AggregationOperation> stages = new ArrayList<>();
        stages.add(matchStage);
        stages.add(sortStage);
        stages.add(Aggregation.skip(pageable.getOffset()));
        stages.add(Aggregation.limit(pageable.getPageSize()));

        // should reducw the amount of data transferred from the database to the application by only projecting the necessary fields -> improve performance (it does not improve performance)
        /*stages.add(Aggregation.project(
            "stationEva", "line",
            "departurePlan", "departureReal",
            "arrivalPlan", "arrivalReal",
            "cancellationTime", "annotations",
            "schoolHoliday", "publicHoliday",
            "dayOfWeek"
        ));*/

        // 1. the mighty $lookup to agregate the weather of the last hours before 'departurePlan' for each trip
        Document lookupStage = new Document("$lookup", new Document()
            .append("from", "weather")
            .append("let", new Document("tripDeparture", "$departurePlan").append("tripStationEva", "$stationEva"))
            .append("pipeline", List.of(
                new Document("$match", new Document("$expr", new Document("$and", List.of(
                    // same station
                    new Document("$eq", List.of("$eva", "$$tripStationEva")),
                    // weather.timestamp <= trip.departurePlan
                    new Document("$lte", List.of("$timestamp", "$$tripDeparture")),
                    // weather.timestamp >= trip.departurePlan - n hours
                    new Document("$gte", List.of(
                        "$timestamp", 
                        new Document("$dateSubtract", new Document()
                            .append("startDate", "$$tripDeparture")
                            .append("unit", "hour")
                            .append("amount", hours)
                        )
                    ))
                )))),
                // Weather Aggregation metrics                
                 new Document("$group", new Document()
                    .append("_id", null)
                    .append("avgTemperature", new Document("$avg", "$temperature"))
                    .append("avgHumidity", new Document("$avg", "$humidity"))
                    .append("totalRain", new Document("$sum", "$rain"))
                    .append("totalSnowfall", new Document("$sum", "$snowfall"))
                    .append("maxWindGusts", new Document("$max", "$windGusts"))
                    .append("avgWindSpeed", new Document("$avg", "$windSpeed"))
                    .append("avgRadiation", new Document("$avg", "$radiation"))
                )
            ))
            .append("as", "weatherAggregation")
        );
        stages.add(context -> lookupStage);

        // 2. Flatten the aggregated array array
        stages.add(Aggregation.unwind("weatherAggregation", true));


        // 4. Execute the pipeline to grab matching candidates
        Aggregation aggregation = Aggregation.newAggregation(stages);
        List<Document> rawResults = mongoTemplate.aggregate(aggregation, TripStopEntity.class, Document.class).getMappedResults();


        // 5. Map raw BSON Documents to DTOs
        List<ReturnAiDataDto> allValidDtos = rawResults.stream().map(doc -> {
            TripStopEntity entity = mongoTemplate.getConverter().read(TripStopEntity.class, doc);
            ReturnAiDataDto dto = ReturnAiDataDto.of(entity);

            // Directly map weather aggregation values on top of our target DTO
            if (doc.containsKey("weatherAggregation") && dto != null) {
                Document weatherDoc = (Document) doc.get("weatherAggregation");
                dto.mapWeatherFields(weatherDoc);
            }

            // Strike check
            if (dto != null) {
                boolean strikeFromDb = isStrikeAffected(entity, relevantStrikes);
                dto.setStrike(dto.isStrike() || strikeFromDb);
            }
            return dto;
        })
        .filter(Objects::nonNull) //Removes all null entries from the final collection stream
        .toList();


        return new PageImpl<>(allValidDtos, pageable, total);
    }

    private boolean isStrikeAffected(TripStopEntity entity, List<StrikeEntity> activeStrikes) {
        if (entity.getDeparturePlan() == null) return false;
        
        return activeStrikes.stream().anyMatch(strike ->

            !entity.getDeparturePlan().isBefore(strike.getStartTime()) &&
            !entity.getDeparturePlan().isAfter(strike.getEndTime()) &&
            // EVA + Linie check
            strike.getAffectedLines().stream().anyMatch(affectedLine ->
                affectedLine.getEva() == entity.getStationEva() &&
                affectedLine.getLines().contains(entity.getLine())
            )
        );
    }
}