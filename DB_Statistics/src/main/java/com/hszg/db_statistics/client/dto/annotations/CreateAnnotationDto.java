package com.hszg.db_statistics.client.dto.annotations;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnnotationDto {

    @Schema(description = "Name/ID of the user creating the annotation", example = "linda")
    @JsonProperty("user_name")
    private String userID;

    @Schema(description = "Service ID of the client", example = "12345")
    @JsonProperty("service_id")
    private String serviceId;

    @Schema(description = "Text of the annotation", example = "nähere Informationen in Kürze")
    @JsonProperty("text")
    private String text;
}