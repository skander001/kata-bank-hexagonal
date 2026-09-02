package fr.shelali.tech.consulting.bank.domain.exception;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;

/**
 * Leve lorsqu'une mise a jour concurrente d'un compte est detectee (verrouillage optimiste).
 */
public class AccountConcurrentUpdateException extends RuntimeException {

    private final AccountNumber accountNumber;

    public AccountConcurrentUpdateException(AccountNumber accountNumber) {
        super("Account was concurrently updated: " + accountNumber);
        this.accountNumber = accountNumber;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }
}
