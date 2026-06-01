package com.example.usermgmt.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    private val basicAuthScheme = "basicAuth"

    @Bean
    fun usermgmtOpenApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("User Management API")
                .version("v1")
                .description(
                    "REST API for the User Management app. " +
                        "Authenticate with HTTP Basic using an application account " +
                        "(e.g. admin@example.com / admin123)."
                )
        )
        .addSecurityItem(SecurityRequirement().addList(basicAuthScheme))
        .components(
            Components().addSecuritySchemes(
                basicAuthScheme,
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic"),
            )
        )
}
