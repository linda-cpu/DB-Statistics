package com.hszg.DB_Management.Trip.Annotation.UserAnnotation.Database;

import org.springframework.data.mongodb.core.mapping.Document;

import com.hszg.DB_Management.Trip.Annotation.Database.AnnotationEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "service_annotations")
//@CompoundIndex(name = "station_eva_source_idx", def = "{'eva': 1, 'source': 1}")
public class UserAnnotationEntity extends AnnotationEntity {
    
    private int eva;
    private String stopId;
    //private String serviceName;
}
