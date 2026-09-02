/**
 * Decorateurs transactionnels des ports entrants (cas d'usage) du compte bancaire.
 *
 * <p>Les services applicatifs de {@code kata-bank-domain} ne portent aucune annotation Spring
 * et ignorent la notion de transaction : chaque appel a {@code AccountRepository} (lecture,
 * puis enregistrement) s'execute par defaut dans sa propre transaction implicite. Pour les cas
 * d'usage qui enchainent une lecture, une decision metier et un enregistrement (depot, retrait,
 * creation), cela romprait le verrouillage optimiste JPA des qu'une transaction englobante
 * serait introduite ailleurs dans l'appel : l'entite relue par {@code save()} deviendrait
 * l'instance managee du meme contexte de persistance, et Hibernate se fierait alors a sa
 * propre version suivie en interne plutot qu'a une version reconstituee manuellement.
 *
 * <p>Les decorateurs de ce paquet encapsulent chaque service applicatif dans une frontiere
 * transactionnelle Spring unique, de sorte que la sequence find -> decide -> save d'un cas
 * d'usage s'execute toujours dans un seul contexte de persistance, quelle que soit la maniere
 * dont le service applicatif est par ailleurs invoque. Ils vivent dans kata-bank-adapter (et
 * non dans kata-bank-domain) car le domaine doit rester exempt de toute dependance Spring.
 */
package fr.shelali.tech.consulting.bank.infrastructure.transaction;
