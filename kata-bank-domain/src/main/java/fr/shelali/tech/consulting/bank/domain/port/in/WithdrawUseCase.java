package fr.shelali.tech.consulting.bank.domain.port.in;

import fr.shelali.tech.consulting.bank.domain.model.Account;

public interface WithdrawUseCase {

    Account withdraw(WithdrawCommand command);
}
