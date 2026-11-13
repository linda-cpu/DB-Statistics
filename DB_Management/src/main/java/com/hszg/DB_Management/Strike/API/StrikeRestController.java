package com.hszg.DB_Management.Strike.API;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hszg.DB_Management.Strike.Database.StrikeEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/strikes")
@Slf4j
@Tag(name = "Strikes", description = "Endpoints for managing strikes")
public class StrikeRestController {

    IStrikeRepository repository;

    public StrikeRestController(IStrikeRepository repository) {
        this.repository = repository;
    }


    /**
     * Creates a new strike with the provided details
     * 
     * @param strikeCreationDto
     */
    @Operation(summary = "Create strike", description = "Creates a new strike with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created strike",
            content = {@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = StrikeReturnDto.class))}
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid JSON structure (text/plain) or validation failed (application/json)",
            content = {
                @Content(mediaType = "application/json"),
                @Content(mediaType = "text/plain")
            }
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
            content = @Content(mediaType = "text/plain", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"))
        ),
        @ApiResponse(responseCode = "500", description = "Internal Server Error - Failed to create strike",
            content = @Content(mediaType = "text/plain", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"))
        )
    })
    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public StrikeReturnDto createStrike(@Valid @RequestBody StrikeCreationDto strikeCreationDto) {
        log.info("Creating strike");
        StrikeEntity strikeEntity = strikeCreationDto.toStrikeEntity();
        repository.save(strikeEntity);
        return new StrikeReturnDto(strikeEntity);
    }


    @Operation(summary = "Get all strikes", description = "Retrieves a list of all strikes stored in the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of strikes"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
            content = {@Content(mediaType = "text/plain", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"))}
        )
    })
    @GetMapping(produces = "application/json")
    public List<StrikeReturnDto> getAllStrikes() {
        log.info("Fetching all strikes from database");
        
        // 1. Fetch all entities from the repository
        List<StrikeEntity> entities = repository.findAll();
        
        // 2. Map the entities into Response DTOs
        return entities.stream()
                .map(entity -> new StrikeReturnDto(entity))
                .toList();
    }

}
