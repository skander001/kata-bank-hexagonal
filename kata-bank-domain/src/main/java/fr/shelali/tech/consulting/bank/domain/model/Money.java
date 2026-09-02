package fr.shelali.tech.consulting.bank.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import fr.shelali.tech.consulting.bank.domain.exception.InvalidAmountException;

/**
 * Montant monetaire : value object immuable enveloppant un {@link BigDecimal} a l'echelle fixe 2.
 *
 * <p>Ne peut jamais representer une valeur negative ou une echelle superieure a 2 : ces cas
 * sont rejetes explicitement plutot que silencieusement corriges (arrondi, clamp, ...).
 */
public final class Money {

    private static final int SCALE = 2;

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount must not be null");
        }
        if (amount.scale() > SCALE) {
            throw new InvalidAmountException("Amount must not have more than " + SCALE + " decimal places");
        }
        if (amount.signum() < 0) {
            throw new InvalidAmountException("Amount must not be negative");
        }
        return new Money(amount.setScale(SCALE));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO.setScale(SCALE));
    }

    public Money add(Money other) {
        requireAmount(other);
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        requireAmount(other);
        return new Money(this.amount.subtract(other.amount));
    }

    public boolean isGreaterThan(Money other) {
        requireAmount(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    public BigDecimal value() {
        return amount;
    }

    private static void requireAmount(Money other) {
        if (other == null) {
            throw new InvalidAmountException("Amount must not be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
