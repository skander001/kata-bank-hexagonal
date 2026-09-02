package fr.shelali.tech.consulting.bank.infrastructure.transaction;

import org.springframework.transaction.annotation.Transactional;

import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.port.in.DepositCommand;
import fr.shelali.tech.consulting.bank.domain.port.in.DepositUseCase;
import lombok.RequiredArgsConstructor;

/**
 * Decore {@link DepositUseCase} d'une frontiere transactionnelle Spring unique. Voir
 * {@code package-info.java} pour la justification.
 */
@RequiredArgsConstructor
public class TransactionalDepositUseCase implements DepositUseCase {

    private final DepositUseCase delegate;

    @Override
    @Transactional
    public Account deposit(DepositCommand command) {
        return delegate.deposit(command);
    }
}
