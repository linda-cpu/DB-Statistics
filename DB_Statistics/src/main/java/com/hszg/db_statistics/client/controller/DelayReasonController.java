package com.hszg.db_statistics.client.controller;

import com.hszg.db_statistics.client.service.DbManagementApiClient;
import com.hszg.db_statistics.client.dto.DelayReasonDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/delayReasons")
@RequiredArgsConstructor
@Tag(name = "Delay Reasons", description = "Get delay reasons")
public class DelayReasonController {
    private final DbManagementApiClient apiClient;

    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('READ_DELAY_REASON')")
    @Operation(summary = "Get Delay Reason", description = "Get Text of delay reason back.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delay reason retrieved successfully" , content = @Content(mediaType = "application/json", schema = @Schema(implementation = DelayReasonDto.class))),
            @ApiResponse(responseCode = "404", description = "Delay reason not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<DelayReasonDto> getDelayReason(
        @Parameter(description = "Code of the delay reason", required = true, example = "1")
        @PathVariable int code
    ) {
        return ResponseEntity.ok(apiClient.getDelayReason(code));
    }


}
