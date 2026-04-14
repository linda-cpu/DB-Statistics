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
@Schema(description = "Response object containing the JWT access token")
public class AuthenticationResponse {

    @Schema(
            description = "The JWT access token used for authenticating subsequent requests",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJMaW5kYSIsImV4cCI6MTY5ODQw..."
    )
    private String token;

    @Schema(description = "Id of the user", example = "1")
    private Long id;

    @Schema(description = "Role of the user")
    private String role;
}