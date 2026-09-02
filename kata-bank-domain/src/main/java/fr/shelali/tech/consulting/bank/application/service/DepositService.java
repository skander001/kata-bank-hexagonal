package fr.shelali.tech.consulting.bank.application.service;

import java.time.Clock;

import fr.shelali.tech.consulting.bank.domain.exception.AccountNotFoundException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.Transaction;
import fr.shelali.tech.consulting.bank.domain.port.in.DepositCommand;
import fr.shelali.tech.consulting.bank.domain.port.in.DepositUseCase;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;
import fr.shelali.tech.consulting.bank.domain.port.out.TransactionRepository;

public class DepositService implements DepositUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public DepositService(AccountRepository accountRepository, TransactionRepository transactionRepository,
                           Clock clock) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Override
    public Account deposit(DepositCommand command) {
        Account account = accountRepository.findByAccountNumber(command.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException(command.accountNumber()));
        Account updated = account.deposit(command.amount());
        Account saved = accountRepository.save(updated);
        transactionRepository.save(Transaction.deposit(
                command.accountNumber(), command.amount(), saved.getBalance(), clock.instant()));
        return saved;
    }
}
