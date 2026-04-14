package com.hszg.db_statistics.client.dto.annotations;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAnnotationDto {
    @Schema(description = "Unique ID of the annotation", example = "annotation-123")
    @JsonProperty("id")
    private String id;

    @Schema(description = "Source of the annotation", example = "API")
    @JsonProperty("source")
    private String source;

    @Schema(description = "Text of the annotation", example = "nähere Informationen in Kürze")
    @JsonProperty("text")
    private String text;

    @Schema(description = "Status code of the annotation", example = "1")
    @JsonProperty("code")
    private int code;

    @Schema(description = "Timestamp of the annotation message", example = "2026-01-26T22:38:40.872Z")
    @JsonProperty("changed_datetime")
    private Instant messageTime;

    private String stop_id;
}