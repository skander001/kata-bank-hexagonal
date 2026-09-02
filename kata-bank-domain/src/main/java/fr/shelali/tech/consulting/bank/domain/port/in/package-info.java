/**
 * Ports entrants (driving ports) : les cas d'usage offerts par l'application.
 *
 * <p>Une interface par cas d'usage plutot qu'une facade unique, pour respecter
 * l'Interface Segregation Principle : un adapter primaire ne depend que du cas
 * d'usage qu'il declenche.
 *
 * <p>Implementes par {@code fr.shelali.tech.consulting.bank.application.service},
 * appeles par les adapters primaires (REST, consumer Kafka).
 */
package fr.shelali.tech.consulting.bank.domain.port.in;
