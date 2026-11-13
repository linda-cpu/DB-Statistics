package com.hszg.db_statistics.client.ml;

import com.hszg.db_statistics.client.ml.dto.PredictionRequestDto;
import com.hszg.db_statistics.client.ml.dto.PredictionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@Tag(name = "ML Predictions", description = "Endpoints for ML-based train delay predictions")
public class PredictionController {

    private final DbMLApiClient mlApiClient;

    @PostMapping("/predict")
    @PreAuthorize("hasAuthority('READ_DATA')") // Oder falls du dein 'PREDICT_DELAY' Privilege behalten willst
    @Operation(summary = "Predict train delay", description = "Uses a neural network model to predict the delay of a train line at a specific station and time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated prediction",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PredictionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters (e.g. past departure time, invalid format)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden access", content = @Content),
            @ApiResponse(responseCode = "502", description = "Upstream ML Service error", content = @Content)
    })
    public ResponseEntity<PredictionResponseDto> predict(
            @Valid @RequestBody PredictionRequestDto requestDto
    ) {
        return ResponseEntity.ok(mlApiClient.predictDelay(requestDto));
    }

    // Dieser Handler fängt den Fehler ab und reicht das JSON 1:1 weiter!
    @ExceptionHandler(MlServiceException.class)
    public ResponseEntity<String> handleMlServiceException(MlServiceException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ex.getErrorBody());
    }
}