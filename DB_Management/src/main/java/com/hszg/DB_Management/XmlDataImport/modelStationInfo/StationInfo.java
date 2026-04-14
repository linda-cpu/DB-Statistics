package com.hszg.DB_Management.XmlDataImport.modelStationInfo;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "stations")
@XmlAccessorType(XmlAccessType.FIELD)
public class StationInfo {

    @XmlElement(name = "station")
    private List<Station> station;
}