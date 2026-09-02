package fr.shelali.tech.consulting.bank.domain.port.in;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountStatement;

public interface GetAccountStatementUseCase {

    AccountStatement getStatement(AccountNumber accountNumber);
}
