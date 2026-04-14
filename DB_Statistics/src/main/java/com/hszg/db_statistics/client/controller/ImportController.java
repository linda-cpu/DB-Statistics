package com.hszg.db_statistics.client.controller;

import com.hszg.db_statistics.client.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
@Tag(name = "Imports", description = "Start manual data imports")
public class ImportController {
    private final ImportService importService;

    @PostMapping
    @PreAuthorize("hasAuthority('INIT_WRITE')")
    @Operation(summary = "Import trips", description = "Starts a manual import of trip data from the DB Management API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import started successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> importTrips() {
        importService.importData();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/xml")
    @PreAuthorize("hasAuthority('INIT_WRITE')")
    @Operation(summary = "Upload XML-Files")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload was successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<String>> uploadXMLFiles(
            @RequestPart("files") MultipartFile[] files
    ) {
        try {
            List<String> result = importService.uploadXMLFiles(files);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
