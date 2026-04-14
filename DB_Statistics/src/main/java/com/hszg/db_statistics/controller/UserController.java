package com.hszg.db_statistics.controller;

import com.hszg.db_statistics.dto.UpdateUserDto;
import com.hszg.db_statistics.dto.UserDto;
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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations related to user administration")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
        public ResponseEntity<Iterable<UserDto>> getAllUsers() {
                return ResponseEntity.ok(userService.getAllUsers());
        }

    @Operation(
            summary = "Delete a user",
            description = "Deletes a specific user by username. Requires ADMIN privileges or being the user themselves."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or #id == authentication.principal.id")
    public ResponseEntity<Void> deleteUser(
            @Parameter(
                    description = "The id of the user to delete",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("User {} not found for deletion", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get user details", description = "Retrieves public information about a specific user.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> getUser(
            @Parameter(
                    description = "The id to search for",
                    example = "1"
            )
            @PathVariable Long id) {
        try {
            UserDto user = userService.getUser(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.warn("User {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update a user", description = "Updates username, role or password.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "New username already taken", content = @Content),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to change role", content = @Content)
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "The id of the user to update", example = "1")
            @PathVariable Long id,
            @RequestBody UpdateUserDto request
    ) {
        UserDto updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{username:[a-zA-Z][a-zA-Z0-9_]*}")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or #username == authentication.name")
    @Operation(summary = "Get user by username", description = "Retrieves public information about a specific user by their username.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    public ResponseEntity<UserDto> getUserByUsername(
            @Parameter(description = "The username of the user to retrieve", example = "linda")
            @PathVariable String username
    ){
       return ResponseEntity.ok(userService.getUserbyUsername(username));
    }
}