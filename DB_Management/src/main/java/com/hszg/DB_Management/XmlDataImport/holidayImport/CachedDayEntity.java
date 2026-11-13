package com.hszg.DB_Management.XmlDataImport.holidayImport;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDate;

@Getter
@Setter
@Document(collection = "holiday_cache")
public class CachedDayEntity {

    @Id
    private String id; // Kombination containing date and SubCode, z.B. "2026-05-07_DE-SN"

    @Indexed
    private LocalDate date;
    
    private String subdivisionCode;
    
    private Boolean publicHoliday;
    private Boolean schoolHoliday;


    @Override
    public String toString() {
        return "CachedDayEntity{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", subdivisionCode='" + subdivisionCode + '\'' +
                ", publicHoliday=" + publicHoliday +
                ", schoolHoliday=" + schoolHoliday +
                '}';
    }
}