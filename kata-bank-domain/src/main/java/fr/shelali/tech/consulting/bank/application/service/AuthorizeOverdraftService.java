package fr.shelali.tech.consulting.bank.application.service;

import fr.shelali.tech.consulting.bank.domain.exception.AccountNotFoundException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.port.in.AuthorizeOverdraftCommand;
import fr.shelali.tech.consulting.bank.domain.port.in.AuthorizeOverdraftUseCase;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;

public class AuthorizeOverdraftService implements AuthorizeOverdraftUseCase {

    private final AccountRepository accountRepository;

    public AuthorizeOverdraftService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account authorizeOverdraft(AuthorizeOverdraftCommand command) {
        Account account = accountRepository.findByAccountNumber(command.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException(command.accountNumber()));
        Account updated = account.authorizeOverdraft(command.overdraftLimit());
        return accountRepository.save(updated);
    }
}
