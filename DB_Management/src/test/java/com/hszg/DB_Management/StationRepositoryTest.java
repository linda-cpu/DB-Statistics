package com.hszg.DB_Management;


import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.Assert;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.hszg.DB_Management.Station.Api.IStationRepository;
import com.hszg.DB_Management.Station.Api.StationRestContoller;
import com.hszg.DB_Management.Station.Api.Dto.StationDto;
import com.hszg.DB_Management.Station.Database.StationEntity;

@SpringBootTest
@Testcontainers
@EnableMongoRepositories(basePackages = "com.hszg.DB_Management.DatabaseConnection")
public class StationRepositoryTest {

	@Container // Starts a real Mongo Docker container
    @ServiceConnection // Automatically wires spring.data.mongodb.uri
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @Autowired
    private IStationRepository repository;
    
    @Test
    void createStation() {
    	StationDto station = new StationDto(80124, "Testbahnhof");
    	//StationRestContoller controller = new StationRestContoller(repository);
    	
    	/*controller.addStation(station);
    	List<StationEntity> result = controller.getStations();
    	System.out.println("stations: " + result.toString());
    	System.out.println("station: " + result.get(0).getStationEva() + " : " + result.get(0).getStationName());*/
    }

    @Test
    void shouldSaveAndRetrieveStation() {
    	
    	/*TrainInfo trainInfo = new TrainInfo();
    	trainInfo.setId(123);
    	trainInfo.setLine("S8");
    	
    	TrainInfo trainInfo2 = new TrainInfo();
    	trainInfo2.setId(234);
    	trainInfo2.setLine("RE1");
    
    	TrainSchedule schedule = new TrainSchedule();
    	schedule.setArrivalPlan(Instant.now());
    	schedule.setArrivalReal(Instant.now());
    	schedule.setDepaturePlan(Instant.now());
    	schedule.setDepatureReal(Instant.now());
    	schedule.setPlattformPlan(1);
    	schedule.setPlattformReal(5);
    	
    	Annotation annotation = new Annotation();
    	annotation.setCode(99);
    	annotation.setMessageTime(Instant.now());
    	annotation.setSource("user1");
    	annotation.setText("Das war ein doofer Zug");
    	
    	Annotation annotation2 = new Annotation();
    	annotation2.setCode(99);
    	annotation2.setMessageTime(Instant.now());
    	annotation2.setSource("user1");
    	annotation2.setText("Mies, mies, mies!");
    	
    	//create trip 1
    	TripStop trip1 = new TripStop();
    	trip1.setTripId("tripId1-2-3");
    	trip1.setTrainInfo(trainInfo);
    	trip1.setSchedule(schedule);
    	trip1.addAnnotation(annotation);
    	trip1.addAnnotation(annotation2);
    	
    	// create trip 2
    	TripStop trip2 = new TripStop();
    	trip2.setTripId("trip id 2-3-4");
    	trip2.setTrainInfo(trainInfo2);
    	trip2.setSchedule(schedule);
    	trip2.setAnnotations(null);
    	
        // 1. Create a Station with nested objects
        Station station = new Station();
        station.setStationEva(8000105);
        station.addTrip(trip1);
        station.addTrip(trip2);
        
        // 2. Save to the real (containerized) DB
        repository.save(station);

        // 3. Retrieve and verify
        Station savedStation = repository.findById(8000105).orElse(null);
        
        assertThat(savedStation).isNotNull();
        assertThat(savedStation.getStationEva()).isEqualTo(8000105);
        System.out.println("Saved Station ID: " + savedStation.getStationEva());
        System.out.println("saved station: " + savedStation.toString());*/
    	
    	
    }
}
