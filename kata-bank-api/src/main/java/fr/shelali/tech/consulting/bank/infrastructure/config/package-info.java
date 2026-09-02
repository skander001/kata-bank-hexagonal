/**
 * Point de composition de l'application.
 *
 * <p>Les services applicatifs du module kata-bank-domain ne portent pas d'annotation
 * Spring. C'est ici qu'ils sont declares en beans, par des methodes
 * {@code @Bean} qui recoivent en parametres les implementations de ports fournies par
 * kata-bank-adapter (persistance) et kata-bank-kafka.
 *
 * <p>Le cablage est donc explicite et lisible en un seul endroit, et le domaine reste
 * instanciable avec un simple {@code new} dans les tests unitaires.
 */
package fr.shelali.tech.consulting.bank.infrastructure.config;
