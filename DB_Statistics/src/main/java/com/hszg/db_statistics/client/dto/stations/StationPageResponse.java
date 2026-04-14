package com.hszg.db_statistics.client.dto.stations;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StationPageResponse {
    private List<StationDto> content;
    
    @Schema(description = "Total number of stations available", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages available", example = "5")
    private int totalPages;

    @Schema(description = "Current page number (0-based index)", example = "0")
    private int size;

    @Schema(description = "Current page number (1-based index)", example = "1")
    private int number;

    @Schema(description = "Number of stations in the current page", example = "true")
    private boolean first;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
}