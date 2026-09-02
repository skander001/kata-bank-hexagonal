package fr.shelali.tech.consulting.bank.persistence;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import fr.shelali.tech.consulting.bank.infrastructure.persistence.AccountRepositoryAdapter;
import fr.shelali.tech.consulting.bank.infrastructure.persistence.entity.AccountEntity;
import fr.shelali.tech.consulting.bank.infrastructure.persistence.repository.AccountJpaRepository;
import fr.shelali.tech.consulting.bank.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountType;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link AccountRepositoryAdapter} against a real PostgreSQL instance
 * (Flyway-migrated schema). Boots the real {@code KataBankApplication} context (default
 * {@code @SpringBootTest} lookup) and reuses this module's shared Testcontainers bootstrap.
 * The persistence adapter itself lives in {@code kata-bank-adapter}, a dependency of this
 * module (kata-bank-api), the Spring Boot composition root.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AccountRepositoryAdapterIT {

    @Autowired
    private AccountRepositoryAdapter accountRepositoryAdapter;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @BeforeEach
    void cleanDatabase() {
        accountJpaRepository.deleteAll();
    }

    @Nested
    class RoundTrip {

        @Test
        void save_then_findByAccountNumber_round_trips_account_number_balance_and_version() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            Account saved = accountRepositoryAdapter.save(account);
            Optional<Account> found = accountRepositoryAdapter.findByAccountNumber(account.getAccountNumber());

            assertThat(found).isPresent();
            assertThat(found.get().getAccountNumber()).isEqualTo(account.getAccountNumber());
            assertThat(found.get().getBalance().value()).isEqualByComparingTo("100.00");
            assertThat(found.get().getVersion()).isEqualTo(saved.getVersion());
        }

        @Test
        void findByAccountNumber_returns_empty_for_an_unknown_account() {
            Optional<Account> found = accountRepositoryAdapter.findByAccountNumber(AccountNumber.generate());

            assertThat(found).isEmpty();
        }

        @Test
        void balance_precision_and_scale_round_trip_exactly() {
            Account account = Account.open(Money.of(new BigDecimal("12345.67")));

            accountRepositoryAdapter.save(account);
            Optional<Account> found = accountRepositoryAdapter.findByAccountNumber(account.getAccountNumber());

            assertThat(found).isPresent();
            assertThat(found.get().getBalance().value()).isEqualByComparingTo("12345.67");
            assertThat(found.get().getBalance().value().scale()).isEqualTo(2);
        }

        @Test
        void save_on_an_existing_account_number_updates_rather_than_duplicates() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));
            accountRepositoryAdapter.save(account);

            Account deposited = account.deposit(Money.of(new BigDecimal("50.00")));
            accountRepositoryAdapter.save(deposited);

            Optional<Account> found = accountRepositoryAdapter.findByAccountNumber(account.getAccountNumber());
            assertThat(found).isPresent();
            assertThat(found.get().getBalance().value()).isEqualByComparingTo("150.00");
            assertThat(accountJpaRepository.findAll()).hasSize(1)
                    .allSatisfy(entity -> assertThat(entity.getAccountNumber())
                            .isEqualTo(account.getAccountNumber().value()));
        }

        @Test
        void negative_balance_within_an_overdraft_limit_round_trips_correctly() {
            Account account = Account.reconstitute(
                    AccountNumber.generate(), Balance.of(new BigDecimal("-30.00")), 0L,
                    Money.of(new BigDecimal("100.00")), AccountType.CHECKING, null);

            accountRepositoryAdapter.save(account);
            Optional<Account> found = accountRepositoryAdapter.findByAccountNumber(account.getAccountNumber());

            assertThat(found).isPresent();
            assertThat(found.get().getBalance().value()).isEqualByComparingTo("-30.00");
            assertThat(found.get().getOverdraftLimit().value()).isEqualByComparingTo("100.00");
        }

        @Test
        void savings_account_round_trips_balance_and_deposit_ceiling() {
            Account account = Account.open(
                    Money.of(new BigDecimal("500.00")), AccountType.SAVINGS, Money.of(new BigDecimal("1000.00")));

            accountRepositoryAdapter.save(account);
            Optional<Account> found = accountRepositoryAdapter.findByAccountNumber(account.getAccountNumber());

            assertThat(found).isPresent();
            assertThat(found.get().getAccountType()).isEqualTo(AccountType.SAVINGS);
            assertThat(found.get().getBalance().value()).isEqualByComparingTo("500.00");
            assertThat(found.get().getDepositCeiling()).contains(Money.of(new BigDecimal("1000.00")));
        }
    }

    @Nested
    class UniqueConstraint {

        @Test
        void duplicate_account_number_insert_violates_the_unique_constraint() {
            String duplicateNumber = AccountNumber.generate().value();
            AccountEntity first = new AccountEntity(UUID.randomUUID(), duplicateNumber, new BigDecimal("10.00"),
                    BigDecimal.ZERO, "CHECKING", null, 0L);
            accountJpaRepository.saveAndFlush(first);

            AccountEntity second = new AccountEntity(UUID.randomUUID(), duplicateNumber, new BigDecimal("20.00"),
                    BigDecimal.ZERO, "CHECKING", null, 0L);

            assertThatThrownBy(() -> accountJpaRepository.saveAndFlush(second))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void adapter_save_updates_in_place_when_the_account_number_already_exists() {
            // AccountRepositoryAdapter#save always finds-by-account-number before deciding
            // insert vs update, so a single-threaded call can never itself trigger the unique
            // constraint: this documents that "already exists" resolves to an update, not a
            // duplicate insert. The true concurrent-insert race (two threads both missing the
            // find before either inserts) is proven under real HTTP concurrency in
            // kata-bank-adapter's integration tests, where the transactional decorators that
            // make the race outcome deterministic are actually wired in.
            Account account = Account.open(Money.of(new BigDecimal("10.00")));
            accountRepositoryAdapter.save(account);

            Account resubmitted = Account.reconstitute(
                    account.getAccountNumber(), Balance.of(new BigDecimal("25.00")), 0L, Money.zero(),
                    AccountType.CHECKING, null);
            accountRepositoryAdapter.save(resubmitted);

            assertThat(accountJpaRepository.findAll()).hasSize(1);
            assertThat(accountRepositoryAdapter.findByAccountNumber(account.getAccountNumber()))
                    .hasValueSatisfying(found -> assertThat(found.getBalance().value()).isEqualByComparingTo("25.00"));
        }
    }

    @Nested
    class OverdraftConstraints {

        @Test
        void balance_below_minus_overdraft_limit_violates_the_check_constraint() {
            AccountEntity entity = new AccountEntity(
                    UUID.randomUUID(), AccountNumber.generate().value(),
                    new BigDecimal("-101.00"), new BigDecimal("100.00"), "CHECKING", null, 0L);

            assertThatThrownBy(() -> accountJpaRepository.saveAndFlush(entity))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void negative_overdraft_limit_violates_the_check_constraint() {
            AccountEntity entity = new AccountEntity(
                    UUID.randomUUID(), AccountNumber.generate().value(),
                    new BigDecimal("10.00"), new BigDecimal("-1.00"), "CHECKING", null, 0L);

            assertThatThrownBy(() -> accountJpaRepository.saveAndFlush(entity))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void balance_exactly_at_minus_overdraft_limit_is_accepted() {
            AccountEntity entity = new AccountEntity(
                    UUID.randomUUID(), AccountNumber.generate().value(),
                    new BigDecimal("-100.00"), new BigDecimal("100.00"), "CHECKING", null, 0L);

            accountJpaRepository.saveAndFlush(entity);

            assertThat(accountJpaRepository.findById(entity.getId())).isPresent();
        }
    }

    @Nested
    class SavingsConstraints {

        @Test
        void savings_without_a_deposit_ceiling_violates_the_check_constraint() {
            AccountEntity entity = new AccountEntity(
                    UUID.randomUUID(), AccountNumber.generate().value(),
                    new BigDecimal("10.00"), BigDecimal.ZERO, "SAVINGS", null, 0L);

            assertThatThrownBy(() -> accountJpaRepository.saveAndFlush(entity))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void checking_with_a_deposit_ceiling_violates_the_check_constraint() {
            AccountEntity entity = new AccountEntity(
                    UUID.randomUUID(), AccountNumber.generate().value(),
                    new BigDecimal("10.00"), BigDecimal.ZERO, "CHECKING", new BigDecimal("1000.00"), 0L);

            assertThatThrownBy(() -> accountJpaRepository.saveAndFlush(entity))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void balance_above_the_deposit_ceiling_violates_the_check_constraint() {
            AccountEntity entity = new AccountEntity(
                    UUID.randomUUID(), AccountNumber.generate().value(),
                    new BigDecimal("1000.01"), BigDecimal.ZERO, "SAVINGS", new BigDecimal("1000.00"), 0L);

            assertThatThrownBy(() -> accountJpaRepository.saveAndFlush(entity))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void balance_exactly_at_the_deposit_ceiling_is_accepted() {
            AccountEntity entity = new AccountEntity(
                    UUID.randomUUID(), AccountNumber.generate().value(),
                    new BigDecimal("1000.00"), BigDecimal.ZERO, "SAVINGS", new BigDecimal("1000.00"), 0L);

            accountJpaRepository.saveAndFlush(entity);

            assertThat(accountJpaRepository.findById(entity.getId())).isPresent();
        }

        @Test
        void savings_with_a_non_zero_overdraft_limit_violates_the_check_constraint() {
            AccountEntity entity = new AccountEntity(
                    UUID.randomUUID(), AccountNumber.generate().value(),
                    new BigDecimal("10.00"), new BigDecimal("50.00"), "SAVINGS", new BigDecimal("1000.00"), 0L);

            assertThatThrownBy(() -> accountJpaRepository.saveAndFlush(entity))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    class OptimisticLocking {

        @Test
        void stale_save_after_a_concurrent_update_fails_with_optimistic_locking_exception() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));
            accountRepositoryAdapter.save(account);
            String accountNumber = account.getAccountNumber().value();

            // Two independent reads of the same row, each in its own persistence context
            // (Spring Data repository methods are transactional per call by default), mirroring
            // two concurrent application instances/threads loading the same account.
            AccountEntity firstRead = accountJpaRepository.findByAccountNumber(accountNumber).orElseThrow();
            AccountEntity secondRead = accountJpaRepository.findByAccountNumber(accountNumber).orElseThrow();

            firstRead.setBalance(new BigDecimal("150.00"));
            accountJpaRepository.saveAndFlush(firstRead);

            secondRead.setBalance(new BigDecimal("70.00"));
            assertThatThrownBy(() -> accountJpaRepository.saveAndFlush(secondRead))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);

            Optional<Account> finalState = accountRepositoryAdapter.findByAccountNumber(account.getAccountNumber());
            assertThat(finalState).isPresent();
            assertThat(finalState.get().getBalance().value()).isEqualByComparingTo("150.00");
        }
    }
}
