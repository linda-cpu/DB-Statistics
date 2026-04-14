package com.hszg.DB_Management.Trip.Annotation.Api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hszg.DB_Management.Trip.Annotation.Api.Dto.AnnotationChangeDto;
import com.hszg.DB_Management.Trip.Annotation.Api.Dto.AnnotationCreationDto;
import com.hszg.DB_Management.Trip.Annotation.Api.Dto.AnnotationReturnDto;
import com.hszg.DB_Management.Trip.Annotation.Database.AnnotationEntity;
import com.hszg.DB_Management.Trip.Annotation.UserAnnotation.API.IUserAnnotationRepository;
import com.hszg.DB_Management.Trip.Annotation.UserAnnotation.API.IUserAnnotationRepository;
import com.hszg.DB_Management.Trip.Annotation.UserAnnotation.Database.UserAnnotationEntity;
import com.hszg.DB_Management.Trip.Annotation.UserAnnotation.Database.UserAnnotationEntity;
import com.hszg.DB_Management.Trip.Api.ITripRepository;
import com.hszg.DB_Management.Trip.Database.TripStopBasicEntity;
import com.hszg.DB_Management.Trip.Database.TripStopEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Annotations", description = "Endpoints for managing user-defined annotations on trip stops.")
@RestController
@RequestMapping("stations/{stationEva}/trips/{id}/annotations")
public class AnnotationRestController {
	
	private final ITripRepository repository;
	private final IUserAnnotationRepository userAnnotationRepository;
	
	public AnnotationRestController(ITripRepository repository, IUserAnnotationRepository userAnnotationRepository) {

		this.repository = repository;
		this.userAnnotationRepository = userAnnotationRepository;
	}
	
	/**
	 * create a new Annotation on a trip/stop
	 * 
	 * @param tripId where the Annotation should be added to
	 * @param userName
	 * @param text
	 * @return 
	 */
	@Operation(summary = "Create a new annotation", description = "Adds a user-defined annotation (with code 98) to a specific trip stop")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully created the annotation",
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationReturnDto.class))}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing", 
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "404", description = "Trip not found", 
			content = {@Content(mediaType = "plain/text")}
		)
	})
	@PostMapping
	public AnnotationReturnDto createAnnotation(
		@Parameter(description = "Annotation details", required = true)
		@Valid @RequestBody AnnotationCreationDto annotation, 
		
		@Parameter(description = "ID of the trip stop to annotate", required = true, example = "-2647381842448452597-2601181303-12")
		@PathVariable String id, 
		
		@Parameter(description = "EVA number of the station", required = true, example = "8010131")
		@PathVariable int stationEva) {

		log.info("Received request to create annotation for ID: {}", id);

		TripStopEntity stopEntity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));

		AnnotationEntity annoEntityToAdd = annotation.toDbAnnotation();
		stopEntity.addAnnotation(annoEntityToAdd);
        TripStopEntity savedStop = repository.save(stopEntity);
		List<AnnotationEntity> annotations = savedStop.getAnnotations();

		// sync service annotation collection
		syncUserAnotationCollection(stationEva, id, annoEntityToAdd);

		// sync user annotation collection
		//syncUserAnnotationCollection(annotation, id, stationEva, annoEntityToAdd, savedStop);

		return AnnotationReturnDto.of(annotations.stream()
			.filter(savedAnno -> savedAnno.getSource().equals(annoEntityToAdd.getSource())
				&& savedAnno.getMessageTime().equals(annoEntityToAdd.getMessageTime()))
			.findFirst()
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve saved annotation")));
	}


	/**
	 * synchronize the user annotation collection with the newly added annotation
	 * 
	 * @param annotation
	 * @param id of trip (not tripId)
	 * @param stationEva
	 * @param annoEntityToAdd
	 */
	/*private void syncUserAnnotationCollection(AnnotationCreationDto annotation, String id, int stationEva,
			AnnotationEntity annoEntityToAdd, TripStopEntity savedStop) {
				
		userAnnotationRepository.findByEvaAndSource(stationEva, annoEntityToAdd.getSource())
			.ifPresentOrElse(
				entity -> {

					// check if trip entry already exists
					Optional<TripStopBasicEntity> existingTrip = entity.getTrips().stream()
						.filter(trip -> trip.getId().equals(id))
						.findFirst();

					if (existingTrip.isPresent()) {
						// add the new annotation to the existing trip
						existingTrip.get().addAnnotation(annoEntityToAdd);
					} else {
						// add the whole object as new trip entry
						entity.getTrips().add(savedStop);
					}
					userAnnotationRepository.save(entity);
				},
				() -> {
					// add new user annotation entity
					UserAnnotationEntity newUserAnnotation = new UserAnnotationEntity();
					newUserAnnotation.setEva(stationEva);
					newUserAnnotation.setSource(annoEntityToAdd.getSource());
					newUserAnnotation.setTrips(new ArrayList<>(List.of(savedStop)));;
					userAnnotationRepository.save(newUserAnnotation);
				});
	}*/

	/**
	 * synchronize the service annotation collection with the newly added annotation
	 * 
	 * @param annotation
	 * @param id
	 * @param stationEva
	 * @param annoEntityToAdd
	 * @param string 
	 * @param savedStop
	 */
	private void syncUserAnotationCollection(int stationEva, String stopId, AnnotationEntity annoEntityToAdd) {
		
			// add new service annotation entity
			UserAnnotationEntity newServiceAnnotation = new UserAnnotationEntity();
			newServiceAnnotation.setEva(stationEva);
			newServiceAnnotation.setStopId(stopId);

			newServiceAnnotation.setId(annoEntityToAdd.getId());
			newServiceAnnotation.setSource(annoEntityToAdd.getSource());
			newServiceAnnotation.setCode(annoEntityToAdd.getCode());
			newServiceAnnotation.setText(annoEntityToAdd.getText());
			newServiceAnnotation.setMessageTime(annoEntityToAdd.getMessageTime());
			
			userAnnotationRepository.save(newServiceAnnotation);
	}
	



	/**
	 * change the text of an existing annotation
	 * 
	 * @param dto
	 * @param id
	 * @param annotationId
	 */
	@Operation(summary = "Update an annotation", description = "Modifies the text and timestamp of an existing annotation")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully updated the annotation", 
			content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationReturnDto.class))}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing", 
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - user is not the author of the annotation", 
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "404", description = "Trip or Annotation not found", 
			content = {@Content(mediaType = "plain/text")}
		)
	})
	@PatchMapping("/{annotationId}")
	public AnnotationReturnDto changeAnnotation(@Valid @RequestBody AnnotationChangeDto dto, @PathVariable String id, @PathVariable String annotationId) {
		log.info("Received request to update annotation ID: {} for trip ID: {}", annotationId, id);
		String source = dto.getServiceId().concat("/").concat(dto.getUserName()); // reconstruct source to check ownership

		TripStopEntity stopEntity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));


		// Use findFirst to get the specific annotation by its ID field
        final boolean foundAnnotation = stopEntity.getAnnotations().stream()
            .filter(a -> a.getId().equals(annotationId))
            .findFirst()
			.map(annotationToUpdate -> {				
				if (annotationToUpdate.getSource().equals(source)) {
					annotationToUpdate.setText(dto.getText());
					annotationToUpdate.setMessageTime(Instant.now());
				} else {
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - user is not the author of the annotation");
				}
				return true;
			}).orElse(false);
            
		// also update the user annotation collection
		userAnnotationRepository.findById(annotationId).ifPresentOrElse(entity -> {
			if (entity.getSource().equals(source)) {
				entity.setText(dto.getText());
				entity.setMessageTime(Instant.now());
				userAnnotationRepository.save(entity);
			} else {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - user is not the author of the annotation");
			}
		}, () -> {
			if (!foundAnnotation) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Annotation not found");
			}
		});

        
        TripStopEntity savedStop = repository.save(stopEntity);
		List<AnnotationEntity> annotations = savedStop.getAnnotations();

		return AnnotationReturnDto.of(annotations.stream()
			.filter(savedAnno -> savedAnno.getId().equals(annotationId))
			.findFirst()
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve saved annotation")));
	}



	/**
	 * Delete the Annotation with the given id
	 * 
	 * @param id
	 */
	@Operation(summary = "Delete an annotation", description = "Removes a specific annotation from a trip stop")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully deleted the annotation", 
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "401", description = "Unauthorized - API Key missing", 
			content = {@Content(mediaType = "plain/text")}
		),
		@ApiResponse(responseCode = "404", description = "Trip or Annotation not found", 
			content = {@Content(mediaType = "plain/text")}
		)
	})
	@DeleteMapping("/{annotationId}")
	public void deleteAnnotation(@PathVariable String id, @PathVariable String annotationId) {
		log.info("Received request to delete annotation ID: {} for trip ID: {}", annotationId, id);

		TripStopEntity stopEntity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));

        boolean removedFromTrips = stopEntity.getAnnotations().removeIf(a -> a.getId().equals(annotationId));

		// also remove from user annotation collection
		boolean removedFromUserAnnotations = userAnnotationRepository.findById(annotationId).map(entity -> {
			userAnnotationRepository.delete(entity);
			return true;
		}).orElse(false);
		
		if (!removedFromTrips) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Annotation not found");
		}
        
        if (!removedFromUserAnnotations && !removedFromTrips) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Annotation not found in user annotations");
        }

        repository.save(stopEntity);
	}

}
