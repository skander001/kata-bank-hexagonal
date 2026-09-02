package fr.shelali.tech.consulting.bank.domain.exception;

/**
 * Leve lorsque la valeur brute d'un type de compte fournie a l'API ne correspond a aucune
 * valeur connue de {@code AccountType} (ni {@code CHECKING} ni {@code SAVINGS}).
 */
public class InvalidAccountTypeException extends RuntimeException {

    private final String rawValue;

    public InvalidAccountTypeException(String rawValue) {
        super("Invalid account type: " + rawValue);
        this.rawValue = rawValue;
    }

    public String getRawValue() {
        return rawValue;
    }
}
