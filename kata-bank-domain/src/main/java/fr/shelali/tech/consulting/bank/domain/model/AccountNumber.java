package fr.shelali.tech.consulting.bank.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Numero de compte : identifiant fonctionnel unique, en format libre.
 *
 * <p>Value object immuable : deux instances portant la meme valeur sont interchangeables.
 */
public final class AccountNumber {

    private final String value;

    public AccountNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Account number must not be null or blank");
        }
        this.value = value;
    }

    public static AccountNumber generate() {
        return new AccountNumber(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountNumber that)) {
            return false;
        }
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
