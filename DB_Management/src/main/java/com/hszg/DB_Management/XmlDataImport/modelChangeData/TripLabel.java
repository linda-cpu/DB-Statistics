package com.hszg.DB_Management.XmlDataImport.modelChangeData;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class TripLabel {

	@XmlAttribute(name = "n")
    private String trainNumber;
	
	@XmlAttribute(name = "c")
    private String trainCategory;
	
}
