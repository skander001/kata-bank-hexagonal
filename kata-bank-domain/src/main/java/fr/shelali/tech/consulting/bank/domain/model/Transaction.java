package fr.shelali.tech.consulting.bank.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Mouvement enregistre sur un compte : fait deja valide, sans regle metier a appliquer ici
 * (la regle metier - solde suffisant, plafond de depot... - a deja ete tranchee par
 * {@link Account} avant que ce mouvement ne soit construit). Seuls des controles de nullite
 * sont effectues, a l'image du style de {@link Account#reconstitute}.
 *
 * <p>Immuable et insere une seule fois : un {@code Transaction} n'est jamais modifie apres sa
 * creation.
 */
public record Transaction(UUID id, AccountNumber accountNumber, TransactionType type, Money amount,
                           Balance balanceAfter, Instant occurredAt) {

    public Transaction {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        if (accountNumber == null) {
            throw new IllegalArgumentException("Account number must not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        if (balanceAfter == null) {
            throw new IllegalArgumentException("Balance after must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at must not be null");
        }
    }

    public static Transaction deposit(AccountNumber accountNumber, Money amount, Balance balanceAfter,
                                       Instant occurredAt) {
        return new Transaction(UUID.randomUUID(), accountNumber, TransactionType.DEPOSIT, amount, balanceAfter,
                occurredAt);
    }

    public static Transaction withdrawal(AccountNumber accountNumber, Money amount, Balance balanceAfter,
                                          Instant occurredAt) {
        return new Transaction(UUID.randomUUID(), accountNumber, TransactionType.WITHDRAWAL, amount, balanceAfter,
                occurredAt);
    }
}
