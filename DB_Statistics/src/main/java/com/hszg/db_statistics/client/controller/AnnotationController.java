package com.hszg.db_statistics.client.controller;

import com.hszg.db_statistics.client.service.DbManagementApiClient;
import com.hszg.db_statistics.client.dto.annotations.AnnotationDto;
import com.hszg.db_statistics.client.dto.annotations.CreateAnnotationDto;
import com.hszg.db_statistics.client.dto.annotations.CreateAnnotationRequest;
import com.hszg.db_statistics.client.dto.annotations.GetAnnotationDto;
import com.hszg.db_statistics.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/stations/{stationEva}/")
@RequiredArgsConstructor
@Tag(name = "Annotations", description = "Annotations for Trips")
public class AnnotationController {

    private final DbManagementApiClient apiClient;
    private final UserService userService;

    @PostMapping("trips/{tripId}/annotations")
    @PreAuthorize("hasAuthority('WRITE_ANNOTATIONS')")
    @Operation(summary = "Get all annotations for a trip at a station", description = "Retrieves all annotations for a specific trip at a specific station.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annotations retrieved successfully" , content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationDto.class))),
            @ApiResponse(responseCode = "404", description = "Station or Trip not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<AnnotationDto> createAnnotation(
        @Parameter(description = "EVA number of the station", required = true, example = "8010131")
        @PathVariable int stationEva, 
        @Parameter(description = "ID of the trip", required = true, example = "-6420897253859089585-2512182347-23")
        @PathVariable String tripId, 
        @RequestBody CreateAnnotationRequest updateDto
    ) {
        CreateAnnotationDto annotationDto = new CreateAnnotationDto();
        annotationDto.setText(updateDto.getText());
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        System.out.println(username);
        annotationDto.setUserID(this.userService.getUserbyUsername(username).getId().toString());
        AnnotationDto created = apiClient.createAnnotation(stationEva, tripId, annotationDto);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("trips/{tripId}/annotations/{id}")
    @PreAuthorize("hasAuthority('WRITE_ANNOTATIONS')")
    @Operation(summary = "Update an annotation for a trip at a station", description = "Updates an existing annotation for a specific trip at a specific station.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annotation updated successfully" , content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnnotationDto.class))),
            @ApiResponse(responseCode = "404", description = "Station, Trip or Annotation not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<AnnotationDto> updateAnnotation(
            @Parameter (description = "EVA number of the station", required = true, example = "8010131")
            @PathVariable int stationEva,
            @Parameter(description = "ID of the trip", required = true, example = "-6420897253859089585-2512182347-23")
            @PathVariable String tripId,
            @Parameter(description = "ID of the annotation", required = true, example = "annotation-12345")
            @PathVariable String id, 
            @RequestBody CreateAnnotationRequest updateDto
    ) {
        CreateAnnotationDto annotationDto = new CreateAnnotationDto();
        annotationDto.setText(updateDto.getText());
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        annotationDto.setUserID(this.userService.getUserbyUsername(username).getId().toString());
        AnnotationDto updated = apiClient.updateAnnotation(stationEva, tripId, id, annotationDto);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("trips/{tripId}/annotations/{id}")
    @PreAuthorize("hasAuthority('WRITE_ANNOTATIONS')")
    @Operation(summary = "Delete an annotation for a trip at a station", description = "Deletes an existing annotation for a specific trip at a specific station.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Annotation deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Station, Trip or Annotation not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<AnnotationDto> deleteAnnotation(
        @Parameter(description = "EVA number of the station", required = true, example = "8010131")
        @PathVariable int stationEva, 
        @Parameter(description = "ID of the trip", required = true, example = "-6420897253859089585-2512182347-23")
        @PathVariable String tripId, 
        @Parameter(description = "ID of the annotation", required = true, example = "annotation-12345")
        @PathVariable String id
    ) {
        apiClient.deleteAnnotation(stationEva, tripId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/annotations")
    @PreAuthorize("hasAuthority('WRITE_ANNOTATIONS')")
    public ResponseEntity<GetAnnotationDto[]> getAnnotation(
            @Parameter(description = "EVA number of the station", required = true, example = "8010131")
            @PathVariable int stationEva
    ) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        Long userId = this.userService.getUserbyUsername(username).getId();
        return ResponseEntity.ok(apiClient.getAnnotations(stationEva, userId));
    }


}
