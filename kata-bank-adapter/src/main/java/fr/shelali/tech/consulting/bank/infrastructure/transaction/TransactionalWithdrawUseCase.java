package fr.shelali.tech.consulting.bank.infrastructure.transaction;

import org.springframework.transaction.annotation.Transactional;

import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.port.in.WithdrawCommand;
import fr.shelali.tech.consulting.bank.domain.port.in.WithdrawUseCase;
import lombok.RequiredArgsConstructor;

/**
 * Decore {@link WithdrawUseCase} d'une frontiere transactionnelle Spring unique. Voir
 * {@code package-info.java} pour la justification : c'est ce qui garantit que la regle
 * metier "un retrait ne peut pas depasser le solde" reste correcte sous concurrence, y compris
 * si une transaction englobante est introduite ailleurs dans l'appel.
 */
@RequiredArgsConstructor
public class TransactionalWithdrawUseCase implements WithdrawUseCase {

    private final WithdrawUseCase delegate;

    @Override
    @Transactional
    public Account withdraw(WithdrawCommand command) {
        return delegate.withdraw(command);
    }
}
