package fr.shelali.tech.consulting.bank.domain.model;

/**
 * Type de compte bancaire : discriminant du comportement metier porte par {@link Account}
 * (plafond de depot, autorisation de decouvert), plutot qu'une hierarchie de classes distinctes.
 */
public enum AccountType {
    CHECKING,
    SAVINGS
}
