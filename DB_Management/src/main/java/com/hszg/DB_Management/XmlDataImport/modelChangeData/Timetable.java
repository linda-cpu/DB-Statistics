package com.hszg.DB_Management.XmlDataImport.modelChangeData;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "timetable")
@XmlAccessorType(XmlAccessType.FIELD)
public class Timetable {
    
    @XmlAttribute
    private String eva;

    @XmlAttribute
    private String station;

    @XmlElement(name = "m")
    private List<Message> messages;
    
    @XmlElement(name = "s")
    private List<Stop> stops;

}
