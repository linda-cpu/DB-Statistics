package com.hszg.DB_Management.DelayReason.API;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hszg.DB_Management.DelayReason.API.Dto.DelayReasonDto;
import com.hszg.DB_Management.DelayReason.Database.DelayReasonEntity;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Tag(name = "Delay reasons", description = "Endpoints for managing delay reasons")
@RestController
@RequestMapping("/delayReason")
public class DelayReasonRestController {
    private IDelayReasonRepository repository;

    public DelayReasonRestController(IDelayReasonRepository repository) {
        this.repository = repository;
    }
    


    /**
     * Get delay reason by code
     * 
     * @param code
     * @return
     */
    @Operation(summary = "Get delay reason", description = "Returns the text description for a specific delay reason code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved delay reason",
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DelayReasonDto.class))}
		),
        @ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
			content = {@Content(mediaType = "plain/text")}
		),
        @ApiResponse(responseCode = "404", description = "Delay reason code not found",
			content = {@Content(mediaType = "plain/text")}
		)
    })
    @GetMapping("/{code}")
    public DelayReasonDto getDelayReason(@PathVariable int code) {
        log.info("Received request for delay reason code: {}", code);

        DelayReasonEntity entity = repository.findById(code)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delay reason code not found"));
        
        return DelayReasonDto.of(entity);
    }


    /**
     * Create a new delay reason
     * 
     * @param dto
     */
    @Hidden
    @Operation(summary = "Create delay reason", description = "Adds a new delay reason code and description to the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully saved",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DelayReasonDto.class))}
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing",
			content = {@Content(mediaType = "plain/text")}
		)
    })
    @PostMapping
    public DelayReasonDto createDelayReason(@Valid @RequestBody DelayReasonDto dto) {
        log.info("Received request to create delay reason with code: {}", dto.getCode());
        
        if (repository.existsById(dto.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Delay reason already exists in the database");
        }
        DelayReasonEntity savedEntity = repository.save(dto.toReasonEntity());
        return DelayReasonDto.of(savedEntity);
    }
}
