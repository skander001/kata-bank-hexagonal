package fr.shelali.tech.consulting.bank.domain.model;

import java.math.BigDecimal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.shelali.tech.consulting.bank.domain.exception.InvalidAmountException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceTest {

    @Nested
    class Of {

        @Test
        void rejects_null_amount() {
            assertThatThrownBy(() -> Balance.of(null))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void accepts_negative_amount() {
            assertThat(Balance.of(new BigDecimal("-50.00")).value()).isEqualByComparingTo("-50.00");
        }

        @Test
        void rejects_scale_greater_than_two() {
            assertThatThrownBy(() -> Balance.of(new BigDecimal("10.001")))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void accepts_zero() {
            assertThat(Balance.of(BigDecimal.ZERO).isZero()).isTrue();
        }

        @Test
        void pads_scale_up_to_two() {
            Balance balance = Balance.of(new BigDecimal("10"));

            assertThat(balance.value()).isEqualByComparingTo("10.00");
        }
    }

    @Test
    void zero_factory_produces_zero_valued_balance() {
        assertThat(Balance.zero().isZero()).isTrue();
        assertThat(Balance.zero().value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Nested
    class Add {

        @Test
        void adds_a_money_amount() {
            Balance result = Balance.of(new BigDecimal("10.50")).add(Money.of(new BigDecimal("5.25")));

            assertThat(result.value()).isEqualByComparingTo("15.75");
        }

        @Test
        void rejects_null_operand() {
            assertThatThrownBy(() -> Balance.of(BigDecimal.TEN).add(null))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    class Subtract {

        @Test
        void subtracts_a_money_amount() {
            Balance result = Balance.of(new BigDecimal("10.50")).subtract(Money.of(new BigDecimal("5.25")));

            assertThat(result.value()).isEqualByComparingTo("5.25");
        }

        @Test
        void can_go_negative() {
            Balance result = Balance.of(new BigDecimal("10.00")).subtract(Money.of(new BigDecimal("30.00")));

            assertThat(result.value()).isEqualByComparingTo("-20.00");
        }

        @Test
        void rejects_null_operand() {
            assertThatThrownBy(() -> Balance.of(BigDecimal.TEN).subtract(null))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    class IsLessThan {

        @Test
        void true_when_strictly_lesser() {
            assertThat(Balance.of(new BigDecimal("-20.01")).isLessThan(Balance.of(new BigDecimal("-20.00"))))
                    .isTrue();
        }

        @Test
        void false_when_equal() {
            assertThat(Balance.of(BigDecimal.TEN).isLessThan(Balance.of(new BigDecimal("10.00")))).isFalse();
        }

        @Test
        void false_when_greater() {
            assertThat(Balance.of(BigDecimal.TEN).isLessThan(Balance.of(BigDecimal.ONE))).isFalse();
        }

        @Test
        void rejects_null_operand() {
            assertThatThrownBy(() -> Balance.of(BigDecimal.TEN).isLessThan(null))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    class IsGreaterThan {

        @Test
        void true_when_strictly_greater() {
            assertThat(Balance.of(new BigDecimal("10.01")).isGreaterThan(Money.of(BigDecimal.TEN))).isTrue();
        }

        @Test
        void false_when_equal() {
            assertThat(Balance.of(BigDecimal.TEN).isGreaterThan(Money.of(new BigDecimal("10.00")))).isFalse();
        }

        @Test
        void false_when_lesser() {
            assertThat(Balance.of(BigDecimal.ONE).isGreaterThan(Money.of(BigDecimal.TEN))).isFalse();
        }

        @Test
        void rejects_null_operand() {
            assertThatThrownBy(() -> Balance.of(BigDecimal.TEN).isGreaterThan(null))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }

    @Nested
    class IsZero {

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.00", "0.0"})
        void true_for_zero_values(String value) {
            assertThat(Balance.of(new BigDecimal(value)).isZero()).isTrue();
        }

        @Test
        void false_for_non_zero_value() {
            assertThat(Balance.of(BigDecimal.ONE).isZero()).isFalse();
        }
    }

    @Nested
    class EqualityAndHashCode {

        @Test
        void equal_by_value_regardless_of_scale_representation() {
            Balance a = Balance.of(new BigDecimal("10"));
            Balance b = Balance.of(new BigDecimal("10.00"));

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        void not_equal_for_different_values() {
            assertThat(Balance.of(BigDecimal.TEN)).isNotEqualTo(Balance.of(BigDecimal.ONE));
        }

        @Test
        void not_equal_to_null_or_other_type() {
            assertThat(Balance.of(BigDecimal.TEN)).isNotEqualTo(null);
            assertThat(Balance.of(BigDecimal.TEN)).isNotEqualTo("10.00");
        }
    }
}
