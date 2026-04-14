package com.hszg.DB_Management.DelayReason.API.Dto;

import com.hszg.DB_Management.DelayReason.Database.DelayReasonEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelayReasonDto {

	private int code;
	private String reason;


    public static DelayReasonDto of(DelayReasonEntity entity) {
        DelayReasonDto dto = new DelayReasonDto();
        dto.setCode(entity.getCode());
        dto.setReason(entity.getReason());
        return dto;
    }


    public DelayReasonEntity toReasonEntity() {
        DelayReasonEntity entity = new DelayReasonEntity();
        entity.setCode(this.code);
        entity.setReason(this.reason);
        return entity;
    }
}
