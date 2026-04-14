package com.hszg.DB_Management.XmlDataImport.modelStationInfo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Station {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String eva;

}
