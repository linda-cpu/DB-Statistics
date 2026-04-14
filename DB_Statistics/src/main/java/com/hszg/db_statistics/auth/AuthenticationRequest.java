package com.hszg.db_statistics.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for user authentication (Login)")
public class AuthenticationRequest {

    @Schema(description = "The username of the user", example = "linda")
    private String username;

    @Schema(description = "The password of the user", example = "secret123")
    private String password;
}