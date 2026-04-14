package com.hszg.DB_Management.XmlDataImport.modelChangeData;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Message {

	@XmlAttribute(name = "c")
    private String code;

	@XmlAttribute(name = "int")
	private String content;

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String from;

    @XmlAttribute
    private String to;

    @XmlAttribute(name = "ts")
    private String timestampCreation;

    @XmlAttribute(name = "pr")
    private Integer priority;

}
