package fr.shelali.tech.consulting.bank.domain.model;

import java.math.BigDecimal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fr.shelali.tech.consulting.bank.domain.exception.DepositCeilingExceededException;
import fr.shelali.tech.consulting.bank.domain.exception.InsufficientBalanceException;
import fr.shelali.tech.consulting.bank.domain.exception.InvalidAmountException;
import fr.shelali.tech.consulting.bank.domain.exception.OverdraftNotAllowedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Nested
    class Open {

        @Test
        void happy_path_with_initial_balance() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            assertThat(account.getBalance().value()).isEqualByComparingTo("100.00");
            assertThat(account.getAccountNumber().value()).isNotBlank();
            assertThat(account.getVersion()).isZero();
        }

        @Test
        void defaults_to_zero_balance_when_initial_balance_is_null() {
            Account account = Account.open(null);

            assertThat(account.getBalance().isZero()).isTrue();
        }

        @Test
        void defaults_to_zero_overdraft_limit() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            assertThat(account.getOverdraftLimit().isZero()).isTrue();
        }

        @Test
        void generates_a_distinct_account_number_per_account() {
            Account a = Account.open(Money.zero());
            Account b = Account.open(Money.zero());

            assertThat(a.getAccountNumber()).isNotEqualTo(b.getAccountNumber());
        }
    }

    @Nested
    class Reconstitute {

        @Test
        void rebuilds_an_account_from_persisted_state() {
            AccountNumber accountNumber = AccountNumber.generate();
            Account account = Account.reconstitute(
                    accountNumber, Balance.of(new BigDecimal("50.00")), 3L, Money.of(new BigDecimal("200.00")),
                    AccountType.CHECKING, null);

            assertThat(account.getAccountNumber()).isEqualTo(accountNumber);
            assertThat(account.getBalance().value()).isEqualByComparingTo("50.00");
            assertThat(account.getVersion()).isEqualTo(3L);
            assertThat(account.getOverdraftLimit().value()).isEqualByComparingTo("200.00");
        }

        @Test
        void rejects_null_account_number() {
            assertThatThrownBy(() -> Account.reconstitute(
                    null, Balance.zero(), 0L, Money.zero(), AccountType.CHECKING, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejects_null_balance() {
            assertThatThrownBy(() -> Account.reconstitute(
                    AccountNumber.generate(), null, 0L, Money.zero(), AccountType.CHECKING, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejects_null_account_type() {
            assertThatThrownBy(() -> Account.reconstitute(
                    AccountNumber.generate(), Balance.zero(), 0L, Money.zero(), null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void defaults_to_zero_overdraft_limit_when_null() {
            Account account = Account.reconstitute(
                    AccountNumber.generate(), Balance.zero(), 0L, null, AccountType.CHECKING, null);

            assertThat(account.getOverdraftLimit().isZero()).isTrue();
        }
    }

    @Nested
    class Deposit {

        @Test
        void happy_path_increases_balance() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            Account updated = account.deposit(Money.of(new BigDecimal("50.00")));

            assertThat(updated.getBalance().value()).isEqualByComparingTo("150.00");
        }

        @Test
        void rejects_null_amount() {
            Account account = Account.open(Money.zero());

            assertThatThrownBy(() -> account.deposit(null))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void rejects_zero_amount() {
            Account account = Account.open(Money.zero());

            assertThatThrownBy(() -> account.deposit(Money.zero()))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void rejects_negative_amount_construction_before_reaching_deposit() {
            // Money itself rejects negative amounts at construction time; deposit can never
            // even be called with one, but we assert the guard is exercised end to end.
            assertThatThrownBy(() -> Money.of(new BigDecimal("-1.00")))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void does_not_mutate_the_original_instance() {
            Account original = Account.open(Money.of(new BigDecimal("100.00")));

            Account updated = original.deposit(Money.of(new BigDecimal("50.00")));

            assertThat(original.getBalance().value()).isEqualByComparingTo("100.00");
            assertThat(updated).isNotSameAs(original);
        }
    }

    @Nested
    class Withdraw {

        @Test
        void happy_path_decreases_balance() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            Account updated = account.withdraw(Money.of(new BigDecimal("30.00")));

            assertThat(updated.getBalance().value()).isEqualByComparingTo("70.00");
        }

        @Test
        void boundary_withdraw_exactly_the_balance_succeeds_and_zeroes_it_out() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            Account updated = account.withdraw(Money.of(new BigDecimal("100.00")));

            assertThat(updated.getBalance().isZero()).isTrue();
        }

        @Test
        void rejects_amount_one_cent_over_the_balance() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            assertThatThrownBy(() -> account.withdraw(Money.of(new BigDecimal("100.01"))))
                    .isInstanceOf(InsufficientBalanceException.class);
        }

        @Test
        void rejects_null_amount() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            assertThatThrownBy(() -> account.withdraw(null))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void rejects_zero_amount() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            assertThatThrownBy(() -> account.withdraw(Money.zero()))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void rejects_withdrawal_from_a_zero_balance_account() {
            Account account = Account.open(Money.zero());

            assertThatThrownBy(() -> account.withdraw(Money.of(new BigDecimal("0.01"))))
                    .isInstanceOf(InsufficientBalanceException.class);
        }

        @Test
        void insufficient_balance_exception_carries_account_number_balance_and_requested_amount() {
            Account account = Account.open(Money.of(new BigDecimal("10.00")));

            assertThatThrownBy(() -> account.withdraw(Money.of(new BigDecimal("20.00"))))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .satisfies(ex -> {
                        InsufficientBalanceException insufficientBalanceException = (InsufficientBalanceException) ex;
                        assertThat(insufficientBalanceException.getAccountNumber()).isEqualTo(account.getAccountNumber());
                        assertThat(insufficientBalanceException.getBalance().value()).isEqualByComparingTo("10.00");
                        assertThat(insufficientBalanceException.getRequestedAmount().value()).isEqualByComparingTo("20.00");
                    });
        }

        @Test
        void does_not_mutate_the_original_instance() {
            Account original = Account.open(Money.of(new BigDecimal("100.00")));

            Account updated = original.withdraw(Money.of(new BigDecimal("30.00")));

            assertThat(original.getBalance().value()).isEqualByComparingTo("100.00");
            assertThat(updated).isNotSameAs(original);
        }

        @Test
        void withdrawal_into_overdraft_succeeds_when_within_the_authorized_limit() {
            Account account = Account.reconstitute(
                    AccountNumber.generate(), Balance.of(new BigDecimal("10.00")), 0L, Money.of(new BigDecimal("100.00")),
                    AccountType.CHECKING, null);

            Account updated = account.withdraw(Money.of(new BigDecimal("50.00")));

            assertThat(updated.getBalance().value()).isEqualByComparingTo("-40.00");
        }

        @Test
        void boundary_withdraw_down_to_exactly_minus_the_overdraft_limit_succeeds() {
            Account account = Account.reconstitute(
                    AccountNumber.generate(), Balance.of(new BigDecimal("10.00")), 0L, Money.of(new BigDecimal("100.00")),
                    AccountType.CHECKING, null);

            Account updated = account.withdraw(Money.of(new BigDecimal("110.00")));

            assertThat(updated.getBalance().value()).isEqualByComparingTo("-100.00");
        }

        @Test
        void rejects_withdrawal_one_cent_beyond_the_overdraft_limit() {
            Account account = Account.reconstitute(
                    AccountNumber.generate(), Balance.of(new BigDecimal("10.00")), 0L, Money.of(new BigDecimal("100.00")),
                    AccountType.CHECKING, null);

            assertThatThrownBy(() -> account.withdraw(Money.of(new BigDecimal("110.01"))))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .satisfies(ex -> assertThat(((InsufficientBalanceException) ex).getOverdraftLimit().value())
                            .isEqualByComparingTo("100.00"));
        }

        @Test
        void zero_overdraft_limit_behaves_exactly_like_no_overdraft_authorized() {
            Account account = Account.reconstitute(
                    AccountNumber.generate(), Balance.of(new BigDecimal("10.00")), 0L, Money.zero(),
                    AccountType.CHECKING, null);

            assertThatThrownBy(() -> account.withdraw(Money.of(new BigDecimal("10.01"))))
                    .isInstanceOf(InsufficientBalanceException.class);
        }
    }

    @Nested
    class AuthorizeOverdraft {

        @Test
        void sets_the_overdraft_limit_on_an_account_without_one() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            Account updated = account.authorizeOverdraft(Money.of(new BigDecimal("300.00")));

            assertThat(updated.getOverdraftLimit().value()).isEqualByComparingTo("300.00");
        }

        @Test
        void updates_an_existing_overdraft_limit() {
            Account account = Account.reconstitute(
                    AccountNumber.generate(), Balance.of(new BigDecimal("10.00")), 0L, Money.of(new BigDecimal("100.00")),
                    AccountType.CHECKING, null);

            Account updated = account.authorizeOverdraft(Money.of(new BigDecimal("50.00")));

            assertThat(updated.getOverdraftLimit().value()).isEqualByComparingTo("50.00");
        }

        @Test
        void revokes_the_overdraft_when_the_limit_is_zero() {
            Account account = Account.reconstitute(
                    AccountNumber.generate(), Balance.of(new BigDecimal("10.00")), 0L, Money.of(new BigDecimal("100.00")),
                    AccountType.CHECKING, null);

            Account updated = account.authorizeOverdraft(Money.zero());

            assertThat(updated.getOverdraftLimit().isZero()).isTrue();
        }

        @Test
        void rejects_null_limit() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            assertThatThrownBy(() -> account.authorizeOverdraft(null))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void does_not_mutate_the_original_instance() {
            Account original = Account.open(Money.of(new BigDecimal("100.00")));

            Account updated = original.authorizeOverdraft(Money.of(new BigDecimal("300.00")));

            assertThat(original.getOverdraftLimit().isZero()).isTrue();
            assertThat(updated).isNotSameAs(original);
        }
    }

    @Nested
    class Savings {

        @Test
        void open_rejects_a_savings_account_without_a_deposit_ceiling() {
            assertThatThrownBy(() -> Account.open(Money.zero(), AccountType.SAVINGS, null))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void open_rejects_a_checking_account_with_a_deposit_ceiling() {
            assertThatThrownBy(() -> Account.open(
                    Money.zero(), AccountType.CHECKING, Money.of(new BigDecimal("1000.00"))))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void open_rejects_an_initial_balance_above_the_ceiling() {
            assertThatThrownBy(() -> Account.open(
                    Money.of(new BigDecimal("1000.01")), AccountType.SAVINGS, Money.of(new BigDecimal("1000.00"))))
                    .isInstanceOf(DepositCeilingExceededException.class);
        }

        @Test
        void deposit_within_the_ceiling_succeeds() {
            Account account = Account.open(
                    Money.of(new BigDecimal("100.00")), AccountType.SAVINGS, Money.of(new BigDecimal("1000.00")));

            Account updated = account.deposit(Money.of(new BigDecimal("50.00")));

            assertThat(updated.getBalance().value()).isEqualByComparingTo("150.00");
        }

        @Test
        void deposit_exactly_at_the_ceiling_succeeds() {
            Account account = Account.open(
                    Money.of(new BigDecimal("900.00")), AccountType.SAVINGS, Money.of(new BigDecimal("1000.00")));

            Account updated = account.deposit(Money.of(new BigDecimal("100.00")));

            assertThat(updated.getBalance().value()).isEqualByComparingTo("1000.00");
        }

        @Test
        void deposit_one_cent_over_the_ceiling_is_rejected() {
            Account account = Account.open(
                    Money.of(new BigDecimal("900.00")), AccountType.SAVINGS, Money.of(new BigDecimal("1000.00")));

            assertThatThrownBy(() -> account.deposit(Money.of(new BigDecimal("100.01"))))
                    .isInstanceOf(DepositCeilingExceededException.class);
        }

        @Test
        void authorize_overdraft_is_rejected_even_for_a_zero_limit() {
            Account account = Account.open(
                    Money.of(new BigDecimal("100.00")), AccountType.SAVINGS, Money.of(new BigDecimal("1000.00")));

            assertThatThrownBy(() -> account.authorizeOverdraft(Money.zero()))
                    .isInstanceOf(OverdraftNotAllowedException.class);
        }

        @Test
        void checking_account_regression_still_opens_via_the_short_overload() {
            Account account = Account.open(Money.of(new BigDecimal("100.00")));

            assertThat(account.getAccountType()).isEqualTo(AccountType.CHECKING);
            assertThat(account.getDepositCeiling()).isEmpty();
        }
    }
}
