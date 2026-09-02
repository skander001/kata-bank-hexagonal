package fr.shelali.tech.consulting.bank.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.shelali.tech.consulting.bank.application.service.AuthorizeOverdraftService;
import fr.shelali.tech.consulting.bank.application.service.CreateAccountService;
import fr.shelali.tech.consulting.bank.application.service.DepositService;
import fr.shelali.tech.consulting.bank.application.service.GetAccountService;
import fr.shelali.tech.consulting.bank.application.service.GetAccountStatementService;
import fr.shelali.tech.consulting.bank.application.service.WithdrawService;
import fr.shelali.tech.consulting.bank.domain.port.in.AuthorizeOverdraftUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.CreateAccountUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.DepositUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.GetAccountStatementUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.GetAccountUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.WithdrawUseCase;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;
import fr.shelali.tech.consulting.bank.domain.port.out.TransactionRepository;
import fr.shelali.tech.consulting.bank.infrastructure.transaction.TransactionalAuthorizeOverdraftUseCase;
import fr.shelali.tech.consulting.bank.infrastructure.transaction.TransactionalCreateAccountUseCase;
import fr.shelali.tech.consulting.bank.infrastructure.transaction.TransactionalDepositUseCase;
import fr.shelali.tech.consulting.bank.infrastructure.transaction.TransactionalGetAccountStatementUseCase;
import fr.shelali.tech.consulting.bank.infrastructure.transaction.TransactionalGetAccountUseCase;
import fr.shelali.tech.consulting.bank.infrastructure.transaction.TransactionalWithdrawUseCase;

/**
 * Cablage explicite des services applicatifs du compte bancaire en beans Spring.
 *
 * <p>Les services de {@code kata-bank-domain} ne portent aucune annotation Spring ; c'est ici,
 * dans le module de composition {@code kata-bank-api}, qu'ils sont instancies et branches sur
 * l'implementation du port sortant {@link AccountRepository} fournie par {@code kata-bank-adapter}.
 *
 * <p>Chaque service applicatif est expose non pas directement, mais enveloppe dans le
 * decorateur transactionnel correspondant de {@code infrastructure.transaction} : c'est ce
 * qui garantit que la sequence find -> decide -> save d'un cas d'usage s'execute dans une
 * seule transaction / un seul contexte de persistance, condition necessaire a la correction
 * du verrouillage optimiste JPA (voir le paquet {@code infrastructure.transaction}).
 */
@Configuration
public class AccountUseCaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public CreateAccountUseCase createAccountUseCase(AccountRepository accountRepository) {
        return new TransactionalCreateAccountUseCase(new CreateAccountService(accountRepository));
    }

    @Bean
    public DepositUseCase depositUseCase(AccountRepository accountRepository,
                                          TransactionRepository transactionRepository, Clock clock) {
        return new TransactionalDepositUseCase(new DepositService(accountRepository, transactionRepository, clock));
    }

    @Bean
    public WithdrawUseCase withdrawUseCase(AccountRepository accountRepository,
                                            TransactionRepository transactionRepository, Clock clock) {
        return new TransactionalWithdrawUseCase(new WithdrawService(accountRepository, transactionRepository, clock));
    }

    @Bean
    public GetAccountUseCase getAccountUseCase(AccountRepository accountRepository) {
        return new TransactionalGetAccountUseCase(new GetAccountService(accountRepository));
    }

    @Bean
    public GetAccountStatementUseCase getAccountStatementUseCase(AccountRepository accountRepository,
                                                                   TransactionRepository transactionRepository,
                                                                   Clock clock) {
        return new TransactionalGetAccountStatementUseCase(
                new GetAccountStatementService(accountRepository, transactionRepository, clock));
    }

    @Bean
    public AuthorizeOverdraftUseCase authorizeOverdraftUseCase(AccountRepository accountRepository) {
        return new TransactionalAuthorizeOverdraftUseCase(new AuthorizeOverdraftService(accountRepository));
    }
}
