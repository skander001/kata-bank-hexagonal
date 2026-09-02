/**
 * Couche applicative : orchestration des cas d'usage.
 *
 * <p>Chaque service implemente un port entrant, charge les agregats via les ports
 * sortants, leur delegue la decision metier, puis persiste et publie le resultat.
 * Il coordonne, il ne decide pas : la regle metier reste dans
 * {@code fr.shelali.tech.consulting.bank.domain.model}.
 *
 * <p>Sans annotation Spring : l'exposition en beans se fait par configuration
 * explicite dans le module kata-bank-adapter, ce qui garde ce module testable
 * sans conteneur.
 */
package fr.shelali.tech.consulting.bank.application.service;
