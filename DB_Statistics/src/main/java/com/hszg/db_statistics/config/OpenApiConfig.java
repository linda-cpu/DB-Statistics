package com.hszg.db_statistics.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                description = "Documentation for DB Statistics API",
                title = "DB Statistics API",
                version = "1.0"
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Auth Description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
        @Bean
        public OpenApiCustomizer sortTagsCustomizer() {
                return openApi -> {
                        // 1. Definiere hier exakt deine Wunsch-Reihenfolge
                        List<String> desiredOrder = List.of(
                                "Role Management",
                                "User Management",
                                "Delay Reasons",
                                "Stations",
                                "Annotations",
                                "Statistic Favorites",
                                "Statistics",
                                "Trips",
                                "Authentication");

                        // 2. Wir sortieren die Tags im OpenApi-Objekt neu
                        if (openApi.getTags() != null) {
                                List<Tag> sortedTags = openApi.getTags().stream()
                                        .sorted(Comparator.comparingInt(tag -> {
                                                int index = desiredOrder.indexOf(tag.getName());
                                                // Wenn ein Tag nicht in der Liste ist, kommt er ans Ende (999)
                                                return index >= 0 ? index : 999;
                                        }))
                                        .collect(Collectors.toList()).reversed();

                                openApi.setTags(sortedTags);
                        }
                };
        }
}