/**
 * Ports sortants (driven ports) : ce dont le domaine a besoin du monde exterieur.
 *
 * <p>Persistance, publication d'evenements, horloge, generation d'identifiants...
 * Ces interfaces sont declarees ici, dans le langage du domaine, et implementees
 * par les adapters secondaires (kata-bank-db, kata-bank-kafka).
 *
 * <p>C'est le point d'inversion de dependance (Dependency Inversion Principle) :
 * le domaine possede le contrat, l'infrastructure s'y conforme.
 */
package fr.shelali.tech.consulting.bank.domain.port.out;
