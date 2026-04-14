package com.hszg.DB_Management.TrainLine.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hszg.DB_Management.TrainLine.Api.Dto.GetTrainLineDto;
import com.hszg.DB_Management.TrainLine.Database.LinesPerStationEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("stations/{stationEva}/lines")
public class TrainLineRestController {

    private ITrainLineRepository repository;

    public TrainLineRestController(ITrainLineRepository repository) {
        this.repository = repository;
    }


    @Operation(summary = "Get lines of station", description = "Returns the line information for a specific station")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved the train lines",
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GetTrainLineDto.class))}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing", 
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "404", description = "Station not found", 
			content = {@Content(mediaType = "plain/text")}
		),
        @ApiResponse(responseCode = "404", description = "No lines found for this station", 
			content = {@Content(mediaType = "plain/text")}
		)
	})
    @GetMapping()
    public GetTrainLineDto getTrainLineOfStation(@PathVariable int stationEva) {
        
        
        LinesPerStationEntity entity = repository.findById(stationEva)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No such station in the database."));

        if (entity.getLines().isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No lines found for this station in the database.");
        }
        
        return GetTrainLineDto.of(entity);
    }
    
}
	