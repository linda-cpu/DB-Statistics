package com.hszg.DB_Management.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenAPIConfiguration {
    
    @Bean
    public OpenAPI customOpenAPI(@Value("${app.project.version}") String appVersion) {
        return new OpenAPI()
        .info(new Info()
            .title("Datamanagement API")
            .version(appVersion)
            .description("This is the API documentation for the Datamanagement application.")
        )
        .addSecurityItem(new SecurityRequirement().addList("serviceAuth"))
            .components(new Components()
                .addSecuritySchemes("serviceAuth", new SecurityScheme()
                    .name("SERVICE-API-KEY")
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .description("Service API Key Authentication **Example:** `e2acd009-e0be-4fff-a412-504cae94f106`")));
    }
}