package fr.shelali.tech.consulting.bank;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Infrastructure des tests d'integration.
 *
 * <p>A importer via {@code @Import(TestcontainersConfiguration.class)} depuis un test
 * {@code @SpringBootTest}. {@code @ServiceConnection} renseigne automatiquement l'URL
 * JDBC : aucune propriete a surcharger dans les tests.
 *
 * <p>Cette classe ne demarre rien tant qu'aucun test ne l'importe ; Docker n'est donc
 * requis que pour les tests qui s'en servent.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:17-alpine");
    }

}
