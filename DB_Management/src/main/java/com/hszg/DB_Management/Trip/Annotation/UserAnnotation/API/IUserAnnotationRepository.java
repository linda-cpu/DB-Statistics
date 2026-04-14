package com.hszg.DB_Management.Trip.Annotation.UserAnnotation.API;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hszg.DB_Management.Trip.Annotation.UserAnnotation.Database.UserAnnotationEntity;

@Repository
public interface IUserAnnotationRepository extends MongoRepository<UserAnnotationEntity, String> {

    List<UserAnnotationEntity> findByEvaAndSource(int eva, String source);
}
