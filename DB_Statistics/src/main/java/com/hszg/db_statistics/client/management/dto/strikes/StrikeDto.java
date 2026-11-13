package com.hszg.db_statistics.client.management.dto.strikes;

import com.hszg.db_statistics.client.management.dto.LineDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Strike with time-range, affected Lines and description")
public class StrikeDto {

    @Schema(description = "ID of the Strike", example = "6a0c59864c7513c902624ef3")
    private String id;

    @Schema(description = "List of affected lines")
    private LineDto[] affectedLines;

    @Schema(description = "Description of the strike", example = "Strike by train drivers affecting multiple lines during rush hour")
    private String description;

    @Schema(description = "Start time of the strike", example = "2023-10-01T10:00:00Z")
    private String startTime;

    @Schema(description = "End time of the strike")
    private String endTime;
}