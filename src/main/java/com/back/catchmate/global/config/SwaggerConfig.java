package com.back.catchmate.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("AccessToken");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("AccessToken");

        Server productionServer = new Server();
        productionServer.setUrl("https://catchmate.site");

        Server developServer = new Server();
        developServer.setUrl("http://3.34.130.89:8080");

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("AccessToken", securityScheme))
                .addSecurityItem(securityRequirement)
                .servers(List.of(productionServer, developServer, localServer))
                .info(apiInfo());
    }

        private Info apiInfo() {
        return new Info()
                .title("DEVELOP CATCH-MATE API")
                .description("API documentation for CATCH-MATE application")
                .version("1.0.0");
    }
}
