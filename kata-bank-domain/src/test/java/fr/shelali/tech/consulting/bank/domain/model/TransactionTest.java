package fr.shelali.tech.consulting.bank.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    private static final AccountNumber ACCOUNT_NUMBER = AccountNumber.generate();
    private static final Money AMOUNT = Money.of(new BigDecimal("50.00"));
    private static final Balance BALANCE_AFTER = Balance.of(new BigDecimal("150.00"));
    private static final Instant OCCURRED_AT = Instant.parse("2026-08-31T10:00:00Z");

    @Nested
    class Deposit {

        @Test
        void sets_the_deposit_type_and_a_generated_id() {
            Transaction transaction = Transaction.deposit(ACCOUNT_NUMBER, AMOUNT, BALANCE_AFTER, OCCURRED_AT);

            assertThat(transaction.type()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(transaction.id()).isNotNull();
            assertThat(transaction.accountNumber()).isEqualTo(ACCOUNT_NUMBER);
            assertThat(transaction.amount()).isEqualTo(AMOUNT);
            assertThat(transaction.balanceAfter()).isEqualTo(BALANCE_AFTER);
            assertThat(transaction.occurredAt()).isEqualTo(OCCURRED_AT);
        }

        @Test
        void generates_a_distinct_id_per_transaction() {
            Transaction a = Transaction.deposit(ACCOUNT_NUMBER, AMOUNT, BALANCE_AFTER, OCCURRED_AT);
            Transaction b = Transaction.deposit(ACCOUNT_NUMBER, AMOUNT, BALANCE_AFTER, OCCURRED_AT);

            assertThat(a.id()).isNotEqualTo(b.id());
        }
    }

    @Nested
    class Withdrawal {

        @Test
        void sets_the_withdrawal_type_and_a_generated_id() {
            Transaction transaction = Transaction.withdrawal(ACCOUNT_NUMBER, AMOUNT, BALANCE_AFTER, OCCURRED_AT);

            assertThat(transaction.type()).isEqualTo(TransactionType.WITHDRAWAL);
            assertThat(transaction.id()).isNotNull();
        }
    }

    @Nested
    class Validation {

        @Test
        void rejects_a_null_id() {
            assertThatThrownBy(() -> new Transaction(null, ACCOUNT_NUMBER, TransactionType.DEPOSIT, AMOUNT,
                    BALANCE_AFTER, OCCURRED_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejects_a_null_account_number() {
            assertThatThrownBy(() -> Transaction.deposit(null, AMOUNT, BALANCE_AFTER, OCCURRED_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejects_a_null_type() {
            assertThatThrownBy(() -> new Transaction(
                    java.util.UUID.randomUUID(), ACCOUNT_NUMBER, null, AMOUNT, BALANCE_AFTER, OCCURRED_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejects_a_null_amount() {
            assertThatThrownBy(() -> Transaction.deposit(ACCOUNT_NUMBER, null, BALANCE_AFTER, OCCURRED_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejects_a_null_balance_after() {
            assertThatThrownBy(() -> Transaction.deposit(ACCOUNT_NUMBER, AMOUNT, null, OCCURRED_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejects_a_null_occurred_at() {
            assertThatThrownBy(() -> Transaction.deposit(ACCOUNT_NUMBER, AMOUNT, BALANCE_AFTER, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
