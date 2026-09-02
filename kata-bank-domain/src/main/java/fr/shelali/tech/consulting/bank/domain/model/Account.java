package fr.shelali.tech.consulting.bank.domain.model;

import java.util.Optional;

import fr.shelali.tech.consulting.bank.domain.exception.DepositCeilingExceededException;
import fr.shelali.tech.consulting.bank.domain.exception.InsufficientBalanceException;
import fr.shelali.tech.consulting.bank.domain.exception.InvalidAmountException;
import fr.shelali.tech.consulting.bank.domain.exception.OverdraftNotAllowedException;

/**
 * Compte bancaire : agregat racine.
 *
 * <p>Immuable : {@link #deposit(Money)}, {@link #withdraw(Money)} et
 * {@link #authorizeOverdraft(Money)} retournent une nouvelle instance plutot que de muter
 * l'etat courant. La regle metier centrale est protegee ici, jamais dans les services
 * applicatifs ou les adapters.
 *
 * <p>{@code accountType} discrimine deux comportements narrows plutot que de justifier une
 * hierarchie de classes : un compte {@code SAVINGS} porte un plafond de depot ({@code
 * depositCeiling}) et ne peut jamais avoir de decouvert autorise.
 */
public final class Account {

    private final AccountNumber accountNumber;
    private final Balance balance;
    private final long version;
    private final Money overdraftLimit;
    private final AccountType accountType;
    private final Money depositCeiling;

    private Account(AccountNumber accountNumber, Balance balance, long version, Money overdraftLimit,
                     AccountType accountType, Money depositCeiling) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.version = version;
        this.overdraftLimit = overdraftLimit;
        this.accountType = accountType;
        this.depositCeiling = depositCeiling;
    }

    /**
     * Ouvre un nouveau compte courant ({@link AccountType#CHECKING}) avec un numero genere, une
     * version initiale de 0, aucun decouvert autorise et aucun plafond de depot.
     */
    public static Account open(Money initialBalance) {
        return open(initialBalance, AccountType.CHECKING, null);
    }

    /**
     * Ouvre un nouveau compte avec un numero genere et une version initiale de 0.
     *
     * <p>{@code depositCeiling} est obligatoire pour un compte {@link AccountType#SAVINGS} et
     * interdit pour un compte {@link AccountType#CHECKING}. Si le solde de depart d'un compte
     * epargne depasse deja le plafond, l'ouverture est traitee comme si ce depot initial avait
     * lui-meme depasse le plafond.
     */
    public static Account open(Money initialBalance, AccountType accountType, Money depositCeiling) {
        if (accountType == null) {
            throw new IllegalArgumentException("Account type must not be null");
        }
        validateDepositCeiling(accountType, depositCeiling);
        Money startingBalance = initialBalance == null ? Money.zero() : initialBalance;
        AccountNumber accountNumber = AccountNumber.generate();
        if (accountType == AccountType.SAVINGS && startingBalance.isGreaterThan(depositCeiling)) {
            throw new DepositCeilingExceededException(
                    accountNumber, Balance.zero(), startingBalance, depositCeiling);
        }
        return new Account(accountNumber, Balance.of(startingBalance.value()), 0L, Money.zero(),
                accountType, depositCeiling);
    }

    /**
     * Reconstruit un compte existant a partir de son etat persiste. Reserve aux adapters
     * de persistance : ne doit jamais servir a exprimer une regle metier.
     */
    public static Account reconstitute(AccountNumber accountNumber, Balance balance, long version,
                                        Money overdraftLimit, AccountType accountType, Money depositCeiling) {
        if (accountNumber == null) {
            throw new IllegalArgumentException("Account number must not be null");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Balance must not be null");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("Account type must not be null");
        }
        Money limit = overdraftLimit == null ? Money.zero() : overdraftLimit;
        return new Account(accountNumber, balance, version, limit, accountType, depositCeiling);
    }

    public Account deposit(Money amount) {
        validateAmount(amount);
        Balance newBalance = balance.add(amount);
        if (accountType == AccountType.SAVINGS && newBalance.isGreaterThan(depositCeiling)) {
            throw new DepositCeilingExceededException(accountNumber, balance, amount, depositCeiling);
        }
        return new Account(accountNumber, newBalance, version, overdraftLimit, accountType, depositCeiling);
    }

    /**
     * Retire {@code amount} du solde. Le retrait est autorise jusqu'a epuisement du solde et,
     * si un decouvert est autorise, au-dela : le solde resultant peut descendre jusqu'a
     * {@code -overdraftLimit} inclus. Il est rejete uniquement lorsque le montant demande ferait
     * passer le solde en-deca de ce plancher (equivalent a l'ancienne regle "solde >= 0" quand
     * {@code overdraftLimit} vaut zero).
     */
    public Account withdraw(Money amount) {
        validateAmount(amount);
        Balance newBalance = balance.subtract(amount);
        Balance overdraftFloor = Balance.of(overdraftLimit.value().negate());
        if (newBalance.isLessThan(overdraftFloor)) {
            throw new InsufficientBalanceException(accountNumber, balance, amount, overdraftLimit);
        }
        return new Account(accountNumber, newBalance, version, overdraftLimit, accountType, depositCeiling);
    }

    /**
     * Autorise (ou revoque, avec une limite de zero) un decouvert jusqu'a {@code limit} sur ce
     * compte. Un compte epargne ne peut jamais avoir de decouvert autorise : la demande est
     * rejetee d'emblee, quelle que soit la limite demandee.
     */
    public Account authorizeOverdraft(Money limit) {
        if (accountType == AccountType.SAVINGS) {
            throw new OverdraftNotAllowedException(accountNumber);
        }
        if (limit == null) {
            throw new InvalidAmountException("Overdraft limit must not be null");
        }
        return new Account(accountNumber, balance, version, limit, accountType, depositCeiling);
    }

    private static void validateAmount(Money amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount must not be null");
        }
        if (amount.isZero()) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    private static void validateDepositCeiling(AccountType accountType, Money depositCeiling) {
        if (accountType == AccountType.SAVINGS && depositCeiling == null) {
            throw new InvalidAmountException("Deposit ceiling is required for a savings account");
        }
        if (accountType == AccountType.CHECKING && depositCeiling != null) {
            throw new InvalidAmountException("Deposit ceiling is only applicable to a savings account");
        }
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public Balance getBalance() {
        return balance;
    }

    public long getVersion() {
        return version;
    }

    public Money getOverdraftLimit() {
        return overdraftLimit;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public Optional<Money> getDepositCeiling() {
        return Optional.ofNullable(depositCeiling);
    }
}
