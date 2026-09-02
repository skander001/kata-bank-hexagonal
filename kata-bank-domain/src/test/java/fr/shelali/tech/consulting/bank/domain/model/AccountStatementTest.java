package fr.shelali.tech.consulting.bank.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountStatementTest {

    private static final AccountNumber ACCOUNT_NUMBER = AccountNumber.generate();
    private static final Balance BALANCE = Balance.of(new BigDecimal("100.00"));
    private static final Instant STATEMENT_DATE = Instant.parse("2026-08-31T10:00:00Z");

    private static Transaction transactionAt(Instant occurredAt) {
        return Transaction.deposit(ACCOUNT_NUMBER, Money.of(new BigDecimal("10.00")), BALANCE, occurredAt);
    }

    @Nested
    class Sorting {

        @Test
        void sorts_transactions_reverse_chronologically_regardless_of_input_order() {
            Transaction oldest = transactionAt(Instant.parse("2026-08-01T00:00:00Z"));
            Transaction middle = transactionAt(Instant.parse("2026-08-15T00:00:00Z"));
            Transaction newest = transactionAt(Instant.parse("2026-08-30T00:00:00Z"));

            AccountStatement statement = new AccountStatement(
                    ACCOUNT_NUMBER, AccountType.CHECKING, BALANCE, STATEMENT_DATE,
                    List.of(middle, oldest, newest));

            assertThat(statement.transactions()).containsExactly(newest, middle, oldest);
        }

        @Test
        void null_transactions_list_becomes_an_empty_list() {
            AccountStatement statement = new AccountStatement(
                    ACCOUNT_NUMBER, AccountType.CHECKING, BALANCE, STATEMENT_DATE, null);

            assertThat(statement.transactions()).isEmpty();
        }
    }

    @Nested
    class Validation {

        @Test
        void rejects_a_null_account_number() {
            assertThatThrownBy(() -> new AccountStatement(
                    null, AccountType.CHECKING, BALANCE, STATEMENT_DATE, List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejects_a_null_account_type() {
            assertThatThrownBy(() -> new AccountStatement(
                    ACCOUNT_NUMBER, null, BALANCE, STATEMENT_DATE, List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejects_a_null_balance() {
            assertThatThrownBy(() -> new AccountStatement(
                    ACCOUNT_NUMBER, AccountType.CHECKING, null, STATEMENT_DATE, List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejects_a_null_statement_date() {
            assertThatThrownBy(() -> new AccountStatement(
                    ACCOUNT_NUMBER, AccountType.CHECKING, BALANCE, null, List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
