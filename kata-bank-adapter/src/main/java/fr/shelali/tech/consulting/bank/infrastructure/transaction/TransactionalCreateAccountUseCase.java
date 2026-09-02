package fr.shelali.tech.consulting.bank.infrastructure.transaction;

import org.springframework.transaction.annotation.Transactional;

import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.port.in.CreateAccountCommand;
import fr.shelali.tech.consulting.bank.domain.port.in.CreateAccountUseCase;
import lombok.RequiredArgsConstructor;

/**
 * Decore {@link CreateAccountUseCase} d'une frontiere transactionnelle Spring unique. Voir
 * {@code package-info.java} pour la justification.
 */
@RequiredArgsConstructor
public class TransactionalCreateAccountUseCase implements CreateAccountUseCase {

    private final CreateAccountUseCase delegate;

    @Override
    @Transactional
    public Account createAccount(CreateAccountCommand command) {
        return delegate.createAccount(command);
    }
}
