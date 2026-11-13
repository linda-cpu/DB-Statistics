package com.hszg.db_statistics.client.management.dto.trips;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class TripPageResponse {
    private List<TripDto> content;

    @Schema(description = "Total number of elements", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Current page number", example = "0")
    private int number;

    @JsonProperty("page")
    private void unpackNestedPage(PageInfo pageInfo) {
        if (pageInfo != null) {
            this.totalElements = pageInfo.getTotalElements();
            this.totalPages = pageInfo.getTotalPages();
            this.size = pageInfo.getSize();
            this.number = pageInfo.getNumber();
        }
    }

    // Kleine Helper-Klasse für das Mapping des verschachtelten Objekts
    @Data
    private static class PageInfo {
        private long totalElements;
        private int totalPages;
        private int size;
        private int number;
    }

}