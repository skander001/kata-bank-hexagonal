package fr.shelali.tech.consulting.bank.infrastructure.transaction;

import org.springframework.transaction.annotation.Transactional;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountStatement;
import fr.shelali.tech.consulting.bank.domain.port.in.GetAccountStatementUseCase;
import lombok.RequiredArgsConstructor;

/**
 * Decore {@link GetAccountStatementUseCase} d'une frontiere transactionnelle Spring en lecture
 * seule. Non indispensable a la correction (une seule sequence de lectures, pas de sequence
 * find -> decide -> save), mais garde la lecture du compte et celle des mouvements dans un seul
 * instantane coherent et suit la convention existante d'envelopper chaque cas d'usage.
 */
@RequiredArgsConstructor
public class TransactionalGetAccountStatementUseCase implements GetAccountStatementUseCase {

    private final GetAccountStatementUseCase delegate;

    @Override
    @Transactional(readOnly = true)
    public AccountStatement getStatement(AccountNumber accountNumber) {
        return delegate.getStatement(accountNumber);
    }
}
