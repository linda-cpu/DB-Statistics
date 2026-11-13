package com.hszg.DB_Management.XmlDataImport.holidayImport;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MongoHolidayRepository extends MongoRepository<CachedDayEntity, String> {
    
    List<CachedDayEntity> findByDate(LocalDate date);
}