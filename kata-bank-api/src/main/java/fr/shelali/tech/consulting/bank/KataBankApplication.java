package fr.shelali.tech.consulting.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entree de l'application, point de composition Spring Boot.
 *
 * <p>Place dans le package racine {@code fr.shelali.tech.consulting.bank}, qui est le
 * prefixe commun de tous les modules : le scan de composants et d'entites couvre donc
 * les adapters (kata-bank-adapter, kata-bank-db) sans configuration supplementaire.
 */
@SpringBootApplication
public class KataBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(KataBankApplication.class, args);
    }
}
