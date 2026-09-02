package fr.shelali.tech.consulting.bank.domain.model;

import java.math.BigDecimal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.shelali.tech.consulting.bank.domain.exception.InvalidAmountException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Nested
    class Of {

        @Test
        void rejects_null_amount() {
            assertThatThrownBy(() -> Money.of(null))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void rejects_negative_amount() {
            assertThatThrownBy(() -> Money.of(new BigDecimal("-0.01")))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void rejects_scale_greater_than_two() {
            assertThatThrownBy(() -> Money.of(new BigDecimal("10.001")))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void accepts_zero() {
            assertThat(Money.of(BigDecimal.ZERO).isZero()).isTrue();
        }

        @Test
        void pads_scale_up_to_two() {
            Money money = Money.of(new BigDecimal("10"));

            assertThat(money.value()).isEqualByComparingTo("10.00");
        }
    }

    @Test
    void zero_factory_produces_zero_valued_money() {
        assertThat(Money.zero().isZero()).isTrue();
        assertThat(Money.zero().value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Nested
    class Add {

        @Test
        void adds_two_amounts() {
            Money result = Money.of(new BigDecimal("10.50")).add(Money.of(new BigDecimal("5.25")));

            assertThat(result.value()).isEqualByComparingTo("15.75");
        }

        @Test
        void rejects_null_operand() {
            assertThatThrownBy(() -> Money.of(BigDecimal.TEN).add(null))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    class Subtract {

        @Test
        void subtracts_two_amounts() {
            Money result = Money.of(new BigDecimal("10.50")).subtract(Money.of(new BigDecimal("5.25")));

            assertThat(result.value()).isEqualByComparingTo("5.25");
        }

        @Test
        void rejects_null_operand() {
            assertThatThrownBy(() -> Money.of(BigDecimal.TEN).subtract(null))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    class IsGreaterThan {

        @Test
        void true_when_strictly_greater() {
            assertThat(Money.of(new BigDecimal("10.01")).isGreaterThan(Money.of(BigDecimal.TEN))).isTrue();
        }

        @Test
        void false_when_equal() {
            assertThat(Money.of(BigDecimal.TEN).isGreaterThan(Money.of(new BigDecimal("10.00")))).isFalse();
        }

        @Test
        void false_when_lesser() {
            assertThat(Money.of(BigDecimal.ONE).isGreaterThan(Money.of(BigDecimal.TEN))).isFalse();
        }

        @Test
        void rejects_null_operand() {
            assertThatThrownBy(() -> Money.of(BigDecimal.TEN).isGreaterThan(null))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    class IsZero {

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.00", "0.0"})
        void true_for_zero_values(String value) {
            assertThat(Money.of(new BigDecimal(value)).isZero()).isTrue();
        }

        @Test
        void false_for_non_zero_value() {
            assertThat(Money.of(BigDecimal.ONE).isZero()).isFalse();
        }
    }

    @Nested
    class EqualityAndHashCode {

        @Test
        void equal_by_value_regardless_of_scale_representation() {
            Money a = Money.of(new BigDecimal("10"));
            Money b = Money.of(new BigDecimal("10.00"));

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        void not_equal_for_different_values() {
            assertThat(Money.of(BigDecimal.TEN)).isNotEqualTo(Money.of(BigDecimal.ONE));
        }

        @Test
        void not_equal_to_null_or_other_type() {
            assertThat(Money.of(BigDecimal.TEN)).isNotEqualTo(null);
            assertThat(Money.of(BigDecimal.TEN)).isNotEqualTo("10.00");
        }
    }
}
