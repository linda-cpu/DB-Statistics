package com.hszg.db_statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Public user information")
public class UserDto {

    @Schema(description = "The unique ID", example = "1")
    private Long id;

    @Schema(description = "The unique username", example = "linda")
    private String username;

    @Schema(description = "The assigned system role", example = "USER")
    private String role;
}