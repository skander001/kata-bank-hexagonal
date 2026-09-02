package fr.shelali.tech.consulting.bank.domain.exception;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;

/**
 * Leve lorsqu'une autorisation de decouvert est demandee sur un compte epargne : ce type de
 * compte ne peut jamais avoir de decouvert autorise, quelle que soit la limite demandee.
 */
public class OverdraftNotAllowedException extends RuntimeException {

    private final AccountNumber accountNumber;

    public OverdraftNotAllowedException(AccountNumber accountNumber) {
        super("Overdraft is not allowed for savings account " + accountNumber);
        this.accountNumber = accountNumber;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }
}
