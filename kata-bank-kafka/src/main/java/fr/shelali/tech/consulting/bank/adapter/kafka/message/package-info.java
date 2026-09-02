/**
 * Payloads des messages Kafka.
 *
 * <p>Contrat d'echange versionne, serialise en JSON. Volontairement distinct des
 * evenements du domaine, pour qu'un refactoring interne ne casse pas les consommateurs.
 */
package fr.shelali.tech.consulting.bank.adapter.kafka.message;
