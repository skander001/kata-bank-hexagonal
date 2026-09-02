package fr.shelali.tech.consulting.bank.application.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.shelali.tech.consulting.bank.domain.exception.DepositCeilingExceededException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountType;
import fr.shelali.tech.consulting.bank.domain.model.Money;
import fr.shelali.tech.consulting.bank.domain.port.in.CreateAccountCommand;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private CreateAccountService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new CreateAccountService(accountRepository);
    }

    @Test
    void happy_path_opens_and_persists_an_account_with_the_requested_balance() {
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account created = service.createAccount(
                new CreateAccountCommand(Money.of(new BigDecimal("100.00")), AccountType.CHECKING, null));

        assertThat(created.getBalance().value()).isEqualByComparingTo("100.00");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance().value()).isEqualByComparingTo("100.00");
    }

    @Test
    void defaults_to_a_zero_balance_when_command_balance_is_null() {
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account created = service.createAccount(new CreateAccountCommand(null, AccountType.CHECKING, null));

        assertThat(created.getBalance().isZero()).isTrue();
    }

    @Test
    void returns_the_repository_persisted_instance() {
        Account persisted = Account.open(Money.of(new BigDecimal("100.00")));
        when(accountRepository.save(any(Account.class))).thenReturn(persisted);

        Account created = service.createAccount(
                new CreateAccountCommand(Money.of(new BigDecimal("100.00")), AccountType.CHECKING, null));

        assertThat(created).isSameAs(persisted);
    }

    @Test
    void opens_and_persists_a_savings_account_with_a_deposit_ceiling() {
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account created = service.createAccount(new CreateAccountCommand(
                Money.of(new BigDecimal("500.00")), AccountType.SAVINGS, Money.of(new BigDecimal("1000.00"))));

        assertThat(created.getAccountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(created.getDepositCeiling()).contains(Money.of(new BigDecimal("1000.00")));
    }

    @Test
    void rejects_a_savings_account_whose_initial_balance_exceeds_the_ceiling() {
        assertThatThrownBy(() -> service.createAccount(new CreateAccountCommand(
                Money.of(new BigDecimal("1000.01")), AccountType.SAVINGS, Money.of(new BigDecimal("1000.00")))))
                .isInstanceOf(DepositCeilingExceededException.class);
    }
}
