package com.hszg.db_statistics.client.dto.annotations;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnnotationRequest {

    @Schema(description = "Text of the annotation", example = "nähere Informationen in Kürze")
    @JsonProperty("text")
    private String text;
}