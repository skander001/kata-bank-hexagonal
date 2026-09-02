package fr.shelali.tech.consulting.bank.infrastructure.transaction;

import org.springframework.transaction.annotation.Transactional;

import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.port.in.GetAccountUseCase;
import lombok.RequiredArgsConstructor;

/**
 * Decore {@link GetAccountUseCase} d'une frontiere transactionnelle Spring en lecture seule.
 * Non indispensable a la correction (lecture unique, pas de sequence find -> decide -> save),
 * mais ajoute pour la coherence avec les autres cas d'usage et des semantiques read-committed
 * explicites.
 */
@RequiredArgsConstructor
public class TransactionalGetAccountUseCase implements GetAccountUseCase {

    private final GetAccountUseCase delegate;

    @Override
    @Transactional(readOnly = true)
    public Account getAccount(AccountNumber accountNumber) {
        return delegate.getAccount(accountNumber);
    }
}
