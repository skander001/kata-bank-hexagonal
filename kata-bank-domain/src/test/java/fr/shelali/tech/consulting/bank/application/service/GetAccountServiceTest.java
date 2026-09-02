package fr.shelali.tech.consulting.bank.application.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.shelali.tech.consulting.bank.domain.exception.AccountNotFoundException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountType;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private GetAccountService service;

    @BeforeEach
    void setUp() {
        service = new GetAccountService(accountRepository);
    }

    @Test
    void happy_path_returns_the_found_account() {
        AccountNumber accountNumber = AccountNumber.generate();
        Account existing = Account.reconstitute(accountNumber, Balance.of(new BigDecimal("100.00")), 0L, Money.zero(),
                AccountType.CHECKING, null);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existing));

        Account found = service.getAccount(accountNumber);

        assertThat(found).isSameAs(existing);
    }

    @Test
    void throws_account_not_found_when_absent() {
        AccountNumber accountNumber = AccountNumber.generate();
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAccount(accountNumber))
                .isInstanceOf(AccountNotFoundException.class)
                .satisfies(ex -> assertThat(((AccountNotFoundException) ex).getAccountNumber()).isEqualTo(accountNumber));
    }
}
