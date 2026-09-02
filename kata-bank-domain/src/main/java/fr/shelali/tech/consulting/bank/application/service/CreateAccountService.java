package fr.shelali.tech.consulting.bank.application.service;

import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.Money;
import fr.shelali.tech.consulting.bank.domain.port.in.CreateAccountCommand;
import fr.shelali.tech.consulting.bank.domain.port.in.CreateAccountUseCase;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;

public class CreateAccountService implements CreateAccountUseCase {

    private final AccountRepository accountRepository;

    public CreateAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(CreateAccountCommand command) {
        Money initialBalance = command.initialBalance() == null ? Money.zero() : command.initialBalance();
        Account account = Account.open(initialBalance, command.accountType(), command.depositCeiling());
        return accountRepository.save(account);
    }
}
