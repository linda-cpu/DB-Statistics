package com.hszg.db_statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Data for updating an existing user. All fields are optional.")
public class UpdateUserDto {

    @Schema(description = "New username (optional)", example = "leonie")
    private String username;

    @Schema(description = "New password (optional)", example = "newSecret123!")
    private String password;

    @Schema(description = "New role (optional, requires ADMIN privileges)", example = "USER")
    private String role;
}