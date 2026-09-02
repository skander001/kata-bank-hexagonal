package fr.shelali.tech.consulting.bank.adapter.in.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadonnees exposees par springdoc sur {@code /v3/api-docs} et {@code /swagger-ui.html}
 * (chemins configures dans {@code application.yml}).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kataBankOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kata Bank API")
                        .description("API REST de gestion de comptes bancaires (architecture hexagonale).")
                        .version("v1"));
    }
}
