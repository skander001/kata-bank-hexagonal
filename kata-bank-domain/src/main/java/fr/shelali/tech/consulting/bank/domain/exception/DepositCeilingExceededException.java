package fr.shelali.tech.consulting.bank.domain.exception;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;

/**
 * Leve lorsqu'un depot sur un compte epargne ferait passer le solde au-dela du plafond de
 * depot autorise pour ce compte.
 */
public class DepositCeilingExceededException extends RuntimeException {

    private final AccountNumber accountNumber;
    private final Balance balance;
    private final Money requestedAmount;
    private final Money depositCeiling;

    public DepositCeilingExceededException(AccountNumber accountNumber, Balance balance, Money requestedAmount,
                                            Money depositCeiling) {
        super("Deposit ceiling exceeded for account " + accountNumber
                + ": balance=" + balance + ", requested=" + requestedAmount
                + ", depositCeiling=" + depositCeiling);
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.requestedAmount = requestedAmount;
        this.depositCeiling = depositCeiling;
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

    public Money getDepositCeiling() {
        return depositCeiling;
    }
}
