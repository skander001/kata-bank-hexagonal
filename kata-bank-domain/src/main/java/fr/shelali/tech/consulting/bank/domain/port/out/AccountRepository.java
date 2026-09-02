package fr.shelali.tech.consulting.bank.domain.port.out;

import java.util.Optional;

import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findByAccountNumber(AccountNumber accountNumber);
}
