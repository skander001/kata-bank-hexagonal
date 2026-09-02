package fr.shelali.tech.consulting.bank.domain.model;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountNumberTest {

    @Test
    void rejects_null_value() {
        assertThatThrownBy(() -> new AccountNumber(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_blank_value() {
        assertThatThrownBy(() -> new AccountNumber("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_empty_value() {
        assertThatThrownBy(() -> new AccountNumber(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accepts_a_non_blank_value() {
        AccountNumber accountNumber = new AccountNumber("ACC-123");

        assertThat(accountNumber.value()).isEqualTo("ACC-123");
    }

    @Test
    void generate_produces_a_non_blank_value() {
        assertThat(AccountNumber.generate().value()).isNotBlank();
    }

    @Test
    void generate_produces_distinct_values() {
        Stream<String> generated = IntStream.range(0, 100)
                .mapToObj(i -> AccountNumber.generate().value());

        assertThat(generated).doesNotHaveDuplicates();
    }

    @Test
    void equal_by_value() {
        assertThat(new AccountNumber("ACC-1")).isEqualTo(new AccountNumber("ACC-1"));
        assertThat(new AccountNumber("ACC-1").hashCode()).isEqualTo(new AccountNumber("ACC-1").hashCode());
    }

    @Test
    void not_equal_for_different_values() {
        assertThat(new AccountNumber("ACC-1")).isNotEqualTo(new AccountNumber("ACC-2"));
    }

    @Test
    void toString_returns_the_value() {
        assertThat(new AccountNumber("ACC-1")).hasToString("ACC-1");
    }
}
