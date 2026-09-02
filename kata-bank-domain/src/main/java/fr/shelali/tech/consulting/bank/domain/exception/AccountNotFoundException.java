package fr.shelali.tech.consulting.bank.domain.exception;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;

/**
 * Leve lorsqu'aucun compte ne correspond au numero de compte demande.
 */
public class AccountNotFoundException extends RuntimeException {

    private final AccountNumber accountNumber;

    public AccountNotFoundException(AccountNumber accountNumber) {
        super("Account not found: " + accountNumber);
        this.accountNumber = accountNumber;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }
}
