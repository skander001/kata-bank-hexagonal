package fr.shelali.tech.consulting.bank.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import fr.shelali.tech.consulting.bank.TestcontainersConfiguration;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;
import fr.shelali.tech.consulting.bank.domain.model.Transaction;
import fr.shelali.tech.consulting.bank.infrastructure.persistence.TransactionRepositoryAdapter;
import fr.shelali.tech.consulting.bank.infrastructure.persistence.repository.TransactionJpaRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link TransactionRepositoryAdapter} against a real PostgreSQL
 * instance (Flyway-migrated schema). Mirrors {@link AccountRepositoryAdapterIT}.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TransactionRepositoryAdapterIT {

    @Autowired
    private TransactionRepositoryAdapter transactionRepositoryAdapter;

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionJpaRepository.deleteAll();
    }

    @Nested
    class RoundTrip {

        @Test
        void save_then_find_round_trips_every_field() {
            AccountNumber accountNumber = AccountNumber.generate();
            Instant occurredAt = Instant.parse("2026-08-15T10:00:00Z");
            Transaction transaction = Transaction.deposit(accountNumber, Money.of(new BigDecimal("50.00")),
                    Balance.of(new BigDecimal("150.00")), occurredAt);

            transactionRepositoryAdapter.save(transaction);
            List<Transaction> found = transactionRepositoryAdapter.findByAccountNumberAndOccurredAtBetween(
                    accountNumber, occurredAt.minusSeconds(1), occurredAt.plusSeconds(1));

            assertThat(found).hasSize(1);
            Transaction result = found.get(0);
            assertThat(result.id()).isEqualTo(transaction.id());
            assertThat(result.accountNumber()).isEqualTo(accountNumber);
            assertThat(result.type()).isEqualTo(transaction.type());
            assertThat(result.amount().value()).isEqualByComparingTo("50.00");
            assertThat(result.balanceAfter().value()).isEqualByComparingTo("150.00");
            assertThat(result.occurredAt()).isEqualTo(occurredAt);
        }
    }

    @Nested
    class RangeQuery {

        @Test
        void excludes_transactions_outside_the_window_and_for_a_different_account() {
            AccountNumber accountNumber = AccountNumber.generate();
            AccountNumber otherAccountNumber = AccountNumber.generate();
            Instant from = Instant.parse("2026-08-01T00:00:00Z");
            Instant to = Instant.parse("2026-08-31T00:00:00Z");

            Transaction insideWindow = Transaction.deposit(accountNumber, Money.of(new BigDecimal("10.00")),
                    Balance.of(new BigDecimal("10.00")), Instant.parse("2026-08-15T00:00:00Z"));
            Transaction beforeWindow = Transaction.deposit(accountNumber, Money.of(new BigDecimal("20.00")),
                    Balance.of(new BigDecimal("30.00")), Instant.parse("2026-07-31T23:59:59Z"));
            Transaction afterWindow = Transaction.deposit(accountNumber, Money.of(new BigDecimal("30.00")),
                    Balance.of(new BigDecimal("60.00")), Instant.parse("2026-08-31T00:00:01Z"));
            Transaction otherAccount = Transaction.deposit(otherAccountNumber, Money.of(new BigDecimal("40.00")),
                    Balance.of(new BigDecimal("40.00")), Instant.parse("2026-08-15T00:00:00Z"));

            transactionRepositoryAdapter.save(insideWindow);
            transactionRepositoryAdapter.save(beforeWindow);
            transactionRepositoryAdapter.save(afterWindow);
            transactionRepositoryAdapter.save(otherAccount);

            List<Transaction> found =
                    transactionRepositoryAdapter.findByAccountNumberAndOccurredAtBetween(accountNumber, from, to);

            assertThat(found).extracting(Transaction::id).containsExactly(insideWindow.id());
        }

        @Test
        void a_transaction_exactly_at_each_boundary_is_included() {
            AccountNumber accountNumber = AccountNumber.generate();
            Instant from = Instant.parse("2026-08-01T00:00:00Z");
            Instant to = Instant.parse("2026-08-31T00:00:00Z");

            Transaction atFromBoundary = Transaction.deposit(accountNumber, Money.of(new BigDecimal("10.00")),
                    Balance.of(new BigDecimal("10.00")), from);
            Transaction atToBoundary = Transaction.deposit(accountNumber, Money.of(new BigDecimal("20.00")),
                    Balance.of(new BigDecimal("30.00")), to);

            transactionRepositoryAdapter.save(atFromBoundary);
            transactionRepositoryAdapter.save(atToBoundary);

            List<Transaction> found =
                    transactionRepositoryAdapter.findByAccountNumberAndOccurredAtBetween(accountNumber, from, to);

            assertThat(found).extracting(Transaction::id)
                    .containsExactlyInAnyOrder(atFromBoundary.id(), atToBoundary.id());
        }

        @Test
        void result_order_is_descending_by_occurred_at() {
            AccountNumber accountNumber = AccountNumber.generate();
            Instant from = Instant.parse("2026-08-01T00:00:00Z");
            Instant to = Instant.parse("2026-08-31T00:00:00Z");

            Transaction oldest = Transaction.deposit(accountNumber, Money.of(new BigDecimal("10.00")),
                    Balance.of(new BigDecimal("10.00")), Instant.parse("2026-08-05T00:00:00Z"));
            Transaction middle = Transaction.deposit(accountNumber, Money.of(new BigDecimal("20.00")),
                    Balance.of(new BigDecimal("30.00")), Instant.parse("2026-08-15T00:00:00Z"));
            Transaction newest = Transaction.deposit(accountNumber, Money.of(new BigDecimal("30.00")),
                    Balance.of(new BigDecimal("60.00")), Instant.parse("2026-08-25T00:00:00Z"));

            transactionRepositoryAdapter.save(middle);
            transactionRepositoryAdapter.save(oldest);
            transactionRepositoryAdapter.save(newest);

            List<Transaction> found =
                    transactionRepositoryAdapter.findByAccountNumberAndOccurredAtBetween(accountNumber, from, to);

            assertThat(found).extracting(Transaction::id)
                    .containsExactly(newest.id(), middle.id(), oldest.id());
        }
    }
}
