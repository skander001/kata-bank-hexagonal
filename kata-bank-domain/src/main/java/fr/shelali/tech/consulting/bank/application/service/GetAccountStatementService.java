package fr.shelali.tech.consulting.bank.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import fr.shelali.tech.consulting.bank.domain.exception.AccountNotFoundException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountStatement;
import fr.shelali.tech.consulting.bank.domain.model.Transaction;
import fr.shelali.tech.consulting.bank.domain.port.in.GetAccountStatementUseCase;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;
import fr.shelali.tech.consulting.bank.domain.port.out.TransactionRepository;

/**
 * Assemble le releve de compte : compte introuvable -> {@link AccountNotFoundException}, comme
 * tous les autres cas d'usage ; sinon, solde courant et mouvements du mois glissant
 * {@code [now - 1 mois, now]}.
 *
 * <p>Depend de {@link Clock} plutot que de {@code Instant.now()} directement, pour que la
 * borne du mois glissant soit deterministe et testable.
 */
public class GetAccountStatementService implements GetAccountStatementUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public GetAccountStatementService(AccountRepository accountRepository,
                                       TransactionRepository transactionRepository, Clock clock) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Override
    public AccountStatement getStatement(AccountNumber accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        Instant to = clock.instant();
        Instant from = to.atZone(ZoneOffset.UTC).minusMonths(1).toInstant();
        List<Transaction> transactions =
                transactionRepository.findByAccountNumberAndOccurredAtBetween(accountNumber, from, to);
        return new AccountStatement(accountNumber, account.getAccountType(), account.getBalance(), to, transactions);
    }
}
