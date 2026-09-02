package fr.shelali.tech.consulting.bank.application.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.shelali.tech.consulting.bank.domain.exception.AccountNotFoundException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountType;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;
import fr.shelali.tech.consulting.bank.domain.port.in.AuthorizeOverdraftCommand;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizeOverdraftServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AuthorizeOverdraftService service;

    @BeforeEach
    void setUp() {
        service = new AuthorizeOverdraftService(accountRepository);
    }

    @Test
    void happy_path_authorizes_overdraft_on_an_existing_account() {
        AccountNumber accountNumber = AccountNumber.generate();
        Account existing = Account.reconstitute(accountNumber, Balance.of(new BigDecimal("100.00")), 0L, Money.zero(),
                AccountType.CHECKING, null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account updated = service.authorizeOverdraft(
                new AuthorizeOverdraftCommand(accountNumber, Money.of(new BigDecimal("500.00"))));

        assertThat(updated.getOverdraftLimit().value()).isEqualByComparingTo("500.00");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getOverdraftLimit().value()).isEqualByComparingTo("500.00");
    }

    @Test
    void revokes_an_existing_overdraft_authorization() {
        AccountNumber accountNumber = AccountNumber.generate();
        Account existing = Account.reconstitute(
                accountNumber, Balance.of(new BigDecimal("100.00")), 0L, Money.of(new BigDecimal("500.00")),
                AccountType.CHECKING, null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account updated = service.authorizeOverdraft(new AuthorizeOverdraftCommand(accountNumber, Money.zero()));

        assertThat(updated.getOverdraftLimit().isZero()).isTrue();
    }

    @Test
    void propagates_account_not_found_and_never_saves() {
        AccountNumber accountNumber = AccountNumber.generate();
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authorizeOverdraft(
                new AuthorizeOverdraftCommand(accountNumber, Money.of(new BigDecimal("500.00")))))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).save(any());
    }
}
