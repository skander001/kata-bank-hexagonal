package fr.shelali.tech.consulting.bank.application.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.shelali.tech.consulting.bank.domain.exception.AccountNotFoundException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountStatement;
import fr.shelali.tech.consulting.bank.domain.model.AccountType;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;
import fr.shelali.tech.consulting.bank.domain.model.Transaction;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;
import fr.shelali.tech.consulting.bank.domain.port.out.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountStatementServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-31T10:00:00Z");
    private static final Instant ONE_MONTH_AGO = NOW.atZone(ZoneOffset.UTC).minusMonths(1).toInstant();

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private GetAccountStatementService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new GetAccountStatementService(accountRepository, transactionRepository, fixedClock);
    }

    @Test
    void happy_path_queries_the_exact_rolling_month_window() {
        AccountNumber accountNumber = AccountNumber.generate();
        Account account = Account.reconstitute(accountNumber, Balance.of(new BigDecimal("100.00")), 0L,
                Money.zero(), AccountType.CHECKING, null);
        Transaction transaction = Transaction.deposit(accountNumber, Money.of(new BigDecimal("50.00")),
                Balance.of(new BigDecimal("100.00")), NOW.minusSeconds(60));
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountNumberAndOccurredAtBetween(accountNumber, ONE_MONTH_AGO, NOW))
                .thenReturn(List.of(transaction));

        AccountStatement statement = service.getStatement(accountNumber);

        assertThat(statement.accountNumber()).isEqualTo(accountNumber);
        assertThat(statement.accountType()).isEqualTo(AccountType.CHECKING);
        assertThat(statement.balance()).isEqualTo(account.getBalance());
        assertThat(statement.statementDate()).isEqualTo(NOW);
        assertThat(statement.transactions()).containsExactly(transaction);
    }

    @Test
    void throws_account_not_found_when_absent() {
        AccountNumber accountNumber = AccountNumber.generate();
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatement(accountNumber))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void empty_transaction_list_is_handled() {
        AccountNumber accountNumber = AccountNumber.generate();
        Account account = Account.reconstitute(accountNumber, Balance.of(new BigDecimal("0.00")), 0L,
                Money.zero(), AccountType.CHECKING, null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountNumberAndOccurredAtBetween(eq(accountNumber), any(), any()))
                .thenReturn(List.of());

        AccountStatement statement = service.getStatement(accountNumber);

        assertThat(statement.transactions()).isEmpty();
    }
}
