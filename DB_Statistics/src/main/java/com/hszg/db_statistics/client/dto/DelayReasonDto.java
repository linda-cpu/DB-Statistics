package com.hszg.db_statistics.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Delay reason with its code and description")
public class DelayReasonDto {

    @Schema(description = "Code of the delay reason", example = "1")
    private int code;

    @Schema(description = "The description of the delay reason", example = "nähere Informationen in Kürze")
    private String reason;
}