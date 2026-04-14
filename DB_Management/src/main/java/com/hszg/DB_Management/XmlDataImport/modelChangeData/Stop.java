package com.hszg.DB_Management.XmlDataImport.modelChangeData;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Stop {

	@XmlAttribute
    private String eva;

    @XmlAttribute
    private String id;

    //@XmlElement(name = "tl")
    //private TripLabel tripLabel;
    
    @XmlElement(name = "ar")
    private ArrivalDeparture arrival;
    
    @XmlElement(name = "dp")
    private ArrivalDeparture departure;
    
    @XmlElement(name = "m")
    private List<Message> messages;
    
}
