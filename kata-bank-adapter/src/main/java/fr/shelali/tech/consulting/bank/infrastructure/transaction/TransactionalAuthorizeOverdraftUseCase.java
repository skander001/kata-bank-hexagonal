package fr.shelali.tech.consulting.bank.infrastructure.transaction;

import org.springframework.transaction.annotation.Transactional;

import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.port.in.AuthorizeOverdraftCommand;
import fr.shelali.tech.consulting.bank.domain.port.in.AuthorizeOverdraftUseCase;
import lombok.RequiredArgsConstructor;

/**
 * Decore {@link AuthorizeOverdraftUseCase} d'une frontiere transactionnelle Spring unique. Voir
 * {@code package-info.java} pour la justification : c'est ce qui garantit que la sequence
 * find -> decide -> save de l'autorisation de decouvert s'execute dans un seul contexte de
 * persistance, y compris si une transaction englobante est introduite ailleurs dans l'appel.
 */
@RequiredArgsConstructor
public class TransactionalAuthorizeOverdraftUseCase implements AuthorizeOverdraftUseCase {

    private final AuthorizeOverdraftUseCase delegate;

    @Override
    @Transactional
    public Account authorizeOverdraft(AuthorizeOverdraftCommand command) {
        return delegate.authorizeOverdraft(command);
    }
}
