package com.hszg.db_statistics.client.dto.trips;

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

    @Schema(description = "Indicates if this is the first page", example = "true")
    private boolean first;
    
    @Schema(description = "Indicates if this is the last page", example = "false")
    private boolean last;
}