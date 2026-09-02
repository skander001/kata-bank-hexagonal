package fr.shelali.tech.consulting.bank.domain.port.in;

import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;

public interface GetAccountUseCase {

    Account getAccount(AccountNumber accountNumber);
}
