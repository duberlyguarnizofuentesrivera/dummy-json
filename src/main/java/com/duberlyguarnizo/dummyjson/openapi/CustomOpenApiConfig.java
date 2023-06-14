package com.duberlyguarnizo.dummyjson.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(contact = @Contact(name = "Duberly Guarnizo",
                email = "duberlygfr@gmail.com",
                url = "https://duberlyguarnizo.com"),
                description = "Provides a web endpoint that returns your own JSON object",
                title = "Dummy JSON API Specification",
                version = "1.0"),
        servers = {@Server(url = "http://localhost:8080", description = "Development API")}
)
@SecurityScheme(name = "Authorization Bearer",
        description = "JWT token authentication for request",
        in = SecuritySchemeIn.HEADER,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class CustomOpenApiConfig {
}
