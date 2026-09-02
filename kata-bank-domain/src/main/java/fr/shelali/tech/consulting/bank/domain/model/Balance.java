package fr.shelali.tech.consulting.bank.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import fr.shelali.tech.consulting.bank.domain.exception.InvalidAmountException;

/**
 * Solde d'un compte : value object immuable enveloppant un {@link BigDecimal} a l'echelle
 * fixe 2.
 *
 * <p>A la difference de {@link Money}, qui represente un montant transige (depot, retrait,
 * plafond de decouvert) et doit rester non negatif, {@code Balance} represente l'etat courant
 * du solde d'un compte et peut devenir negatif lorsqu'un decouvert autorise le permet. Seule
 * l'echelle superieure a 2 ou une valeur nulle est rejetee explicitement.
 */
public final class Balance {

    private static final int SCALE = 2;

    private final BigDecimal amount;

    private Balance(BigDecimal amount) {
        this.amount = amount;
    }

    public static Balance of(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount must not be null");
        }
        if (amount.scale() > SCALE) {
            throw new InvalidAmountException("Amount must not have more than " + SCALE + " decimal places");
        }
        return new Balance(amount.setScale(SCALE));
    }

    public static Balance zero() {
        return new Balance(BigDecimal.ZERO.setScale(SCALE));
    }

    public Balance add(Money amount) {
        requireAmount(amount);
        return new Balance(this.amount.add(amount.value()));
    }

    public Balance subtract(Money amount) {
        requireAmount(amount);
        return new Balance(this.amount.subtract(amount.value()));
    }

    public boolean isLessThan(Balance other) {
        requireBalance(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThan(Money other) {
        requireAmount(other);
        return this.amount.compareTo(other.value()) > 0;
    }

    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    public BigDecimal value() {
        return amount;
    }

    private static void requireAmount(Money amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount must not be null");
        }
    }

    private static void requireBalance(Balance other) {
        if (other == null) {
            throw new InvalidAmountException("Amount must not be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Balance balance)) {
            return false;
        }
        return amount.compareTo(balance.amount) == 0;
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
