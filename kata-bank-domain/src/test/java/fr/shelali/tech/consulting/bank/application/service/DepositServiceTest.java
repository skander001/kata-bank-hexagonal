package fr.shelali.tech.consulting.bank.application.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.shelali.tech.consulting.bank.domain.exception.AccountNotFoundException;
import fr.shelali.tech.consulting.bank.domain.exception.InvalidAmountException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountType;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;
import fr.shelali.tech.consulting.bank.domain.model.Transaction;
import fr.shelali.tech.consulting.bank.domain.model.TransactionType;
import fr.shelali.tech.consulting.bank.domain.port.in.DepositCommand;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;
import fr.shelali.tech.consulting.bank.domain.port.out.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-31T10:00:00Z");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private DepositService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new DepositService(accountRepository, transactionRepository, fixedClock);
    }

    @Test
    void happy_path_deposits_into_an_existing_account() {
        AccountNumber accountNumber = AccountNumber.generate();
        Account existing = Account.reconstitute(accountNumber, Balance.of(new BigDecimal("100.00")), 0L, Money.zero(),
                AccountType.CHECKING, null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account updated = service.deposit(new DepositCommand(accountNumber, Money.of(new BigDecimal("50.00"))));

        assertThat(updated.getBalance().value()).isEqualByComparingTo("150.00");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance().value()).isEqualByComparingTo("150.00");
    }

    @Test
    void happy_path_saves_a_deposit_transaction_with_the_saved_balance_and_clock_instant() {
        AccountNumber accountNumber = AccountNumber.generate();
        Account existing = Account.reconstitute(accountNumber, Balance.of(new BigDecimal("100.00")), 0L, Money.zero(),
                AccountType.CHECKING, null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deposit(new DepositCommand(accountNumber, Money.of(new BigDecimal("50.00"))));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();
        assertThat(saved.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(saved.accountNumber()).isEqualTo(accountNumber);
        assertThat(saved.amount().value()).isEqualByComparingTo("50.00");
        assertThat(saved.balanceAfter().value()).isEqualByComparingTo("150.00");
        assertThat(saved.occurredAt()).isEqualTo(NOW);
    }

    @Test
    void propagates_account_not_found_and_never_saves() {
        AccountNumber accountNumber = AccountNumber.generate();
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deposit(new DepositCommand(accountNumber, Money.of(new BigDecimal("50.00")))))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void propagates_invalid_amount_and_never_saves() {
        AccountNumber accountNumber = AccountNumber.generate();
        Account existing = Account.reconstitute(accountNumber, Balance.of(new BigDecimal("100.00")), 0L, Money.zero(),
                AccountType.CHECKING, null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.deposit(new DepositCommand(accountNumber, Money.zero())))
                .isInstanceOf(InvalidAmountException.class);

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}
