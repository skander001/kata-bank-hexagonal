package fr.shelali.tech.consulting.bank.domain.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Releve de compte : type de compte, solde a l'instant du releve, et mouvements du mois
 * glissant qui precede cet instant.
 *
 * <p>Le constructeur compact trie defensivement {@code transactions} par ordre
 * anti-chronologique : l'ordre "du plus recent au plus ancien" est ainsi garanti par l'objet
 * du domaine lui-meme, quel que soit l'ordre dans lequel le port sortant les a retournees.
 */
public record AccountStatement(AccountNumber accountNumber, AccountType accountType, Balance balance,
                                Instant statementDate, List<Transaction> transactions) {

    public AccountStatement {
        if (accountNumber == null) {
            throw new IllegalArgumentException("Account number must not be null");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("Account type must not be null");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Balance must not be null");
        }
        if (statementDate == null) {
            throw new IllegalArgumentException("Statement date must not be null");
        }
        transactions = transactions == null ? List.of()
                : transactions.stream()
                        .sorted(Comparator.comparing(Transaction::occurredAt).reversed())
                        .toList();
    }
}
