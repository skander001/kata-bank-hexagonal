package fr.shelali.tech.consulting.bank.domain.exception;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;

/**
 * Leve lorsqu'un retrait demande un montant qui ferait passer le solde en-deca du plancher
 * autorise (zero, ou {@code -overdraftLimit} si un decouvert est autorise sur le compte).
 */
public class InsufficientBalanceException extends RuntimeException {

    private final AccountNumber accountNumber;
    private final Balance balance;
    private final Money requestedAmount;
    private final Money overdraftLimit;

    public InsufficientBalanceException(AccountNumber accountNumber, Balance balance, Money requestedAmount,
                                         Money overdraftLimit) {
        super("Insufficient balance for account " + accountNumber
                + ": balance=" + balance + ", requested=" + requestedAmount
                + ", overdraftLimit=" + overdraftLimit);
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.requestedAmount = requestedAmount;
        this.overdraftLimit = overdraftLimit;
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public Balance getBalance() {
        return balance;
    }

    public Money getRequestedAmount() {
        return requestedAmount;
    }

    public Money getOverdraftLimit() {
        return overdraftLimit;
    }
}
