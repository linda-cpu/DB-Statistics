package com.hszg.DB_Management.Trip.Annotation.UserAnnotation.API;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hszg.DB_Management.Trip.Annotation.UserAnnotation.API.Dto.UserAnnotationDto;
import com.hszg.DB_Management.Trip.Annotation.UserAnnotation.Database.UserAnnotationEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "User Annotations", description = "Endpoints for requesting trips with annotations of the given user ID.")
@RestController
public class UserAnnotationRestController {

    private final IUserAnnotationRepository repository;

    public UserAnnotationRestController(IUserAnnotationRepository repository) {
        this.repository = repository;
    }

    /**
	 * change the text of an existing annotation
	 * 
	 * @param dto
	 * @param id
	 * @param annotationId
     * @return 
	 */
	@Operation(summary = "get Trips that have annotations of the given service", description = "Returns the annotations for a specific service")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved the trips with annotations", 
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserAnnotationDto.class))}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing", 
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "404", description = "No annotations found for this service at the given station.", 
			content = {@Content(mediaType = "plain/text")}
		)
	})
	@GetMapping("stations/{stationEva}/trips/{serviceId}/{userId}/annotations")
	public List<UserAnnotationDto> getServiceAnnotations(
			@Parameter(description = "eva number of the station", required = true, example = "8010131")
			@PathVariable int stationEva, 
			
			@Parameter(description = "service ID of the service", required = true, example = "e2acd009-e0be-4fff-a412-504cae94f106")
			@PathVariable String serviceId, 
			
			@Parameter(description = "user ID of the user", required = true, example = "user1")
			@PathVariable String userId) {

		log.info("Received request to get trips with annotations of service: {}", serviceId);
        
		String source = serviceId + "/" + userId;
        List<UserAnnotationEntity> entities = repository.findByEvaAndSource(stationEva, source);

		if (entities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No annotations found for user " + userId + " at station " + stationEva + ".");
        }
		
        List<UserAnnotationDto> dtos = UserAnnotationDto.of(entities);

        return dtos;
	}
}
