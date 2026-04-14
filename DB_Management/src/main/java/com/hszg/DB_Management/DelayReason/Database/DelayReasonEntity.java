package com.hszg.DB_Management.DelayReason.Database;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "delayReasons")
public class DelayReasonEntity {

	@Id
	private int code;
	
	@NonNull
	private String reason;
}
