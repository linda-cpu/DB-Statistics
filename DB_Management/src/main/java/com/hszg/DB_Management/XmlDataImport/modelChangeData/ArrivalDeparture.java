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
public class ArrivalDeparture {

	@XmlAttribute(name = "pt")
    private String plannedTime;
	
	@XmlAttribute(name = "ct")
    private String changedTime;
	
	@XmlAttribute(name = "pp")
    private String plannedPlattform;
	
	@XmlAttribute(name = "cp")
    private String changedPlattform;
	
	@XmlAttribute(name = "clt")
    private String cancellationTime;

    @XmlAttribute(name = "l")
    private String line;

    @XmlElement(name = "m")
    private List<Message> messages;

}
