package com.hszg.DB_Management.Trip.Database;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import com.hszg.DB_Management.Trip.Annotation.Database.AnnotationEntity;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripStopBasicEntity {

	@Id
	private String id;

	@NonNull
	private String tripId;
	
	private int stationEva;

	private String stationName;

	private String line;
	
	private Instant arrivalPlan;
	
	private Instant arrivalReal;
	
	@Indexed(name = "departure_plan_idx")
	private Instant departurePlan;
	
	private Instant departureReal;
	
	private int plattformPlan;
	
	private Integer plattformReal;
	
	private Instant cancellationTime;
	
	private List<AnnotationEntity> annotations = new ArrayList<AnnotationEntity>();

	public void addAnnotation(AnnotationEntity annotationEntity) {
		annotations.add(annotationEntity);
	}

	@Override
	public String toString() {
		return "TripStopEntity [id=" + id + ", tripId=" + tripId + ", stationEva=" + stationEva + ", stationName="
				+ stationName + ", line=" + line
				+ ", arrivalPlan=" + arrivalPlan + ", arrivalReal=" + arrivalReal + ", departurePlan=" + departurePlan
				+ ", departureReal=" + departureReal + ", plattformPlan=" + plattformPlan + ", plattformReal="
				+ plattformReal + ", cancellationTime=" + cancellationTime + ", annotations=" + annotations + "]";
	}

	

}
