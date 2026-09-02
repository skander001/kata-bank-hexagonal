package fr.shelali.tech.consulting.bank.domain.port.in;

import fr.shelali.tech.consulting.bank.domain.model.Account;

public interface DepositUseCase {

    Account deposit(DepositCommand command);
}
