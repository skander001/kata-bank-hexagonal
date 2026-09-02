package fr.shelali.tech.consulting.bank.adapter.in.rest.mapper;

import java.math.BigDecimal;
import java.util.Locale;

import org.mapstruct.Mapper;

import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.AccountResponse;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.AccountStatementResponse;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.AuthorizeOverdraftRequest;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.CreateAccountRequest;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.DepositRequest;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.TransactionResponse;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.WithdrawRequest;
import fr.shelali.tech.consulting.bank.domain.exception.InvalidAccountTypeException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountStatement;
import fr.shelali.tech.consulting.bank.domain.model.AccountType;
import fr.shelali.tech.consulting.bank.domain.model.Money;
import fr.shelali.tech.consulting.bank.domain.model.Transaction;
import fr.shelali.tech.consulting.bank.domain.port.in.AuthorizeOverdraftCommand;
import fr.shelali.tech.consulting.bank.domain.port.in.CreateAccountCommand;
import fr.shelali.tech.consulting.bank.domain.port.in.DepositCommand;
import fr.shelali.tech.consulting.bank.domain.port.in.WithdrawCommand;

/**
 * Traduction DTO REST &lt;-&gt; commandes et objets du domaine. Ecrite en methodes {@code
 * default} : les commandes et {@link Account} sont immuables sans setters, hors du modele
 * bean que MapStruct sait generer par introspection.
 */
@Mapper
public interface AccountDtoMapper {

    default CreateAccountCommand toCommand(CreateAccountRequest request) {
        Money initialBalance = request.initialBalance() == null ? null : toMoney(request.initialBalance());
        Money depositCeiling = request.depositCeiling() == null ? null : toMoney(request.depositCeiling());
        return new CreateAccountCommand(initialBalance, toAccountType(request.accountType()), depositCeiling);
    }

    default DepositCommand toDepositCommand(String accountNumber, DepositRequest request) {
        return new DepositCommand(toAccountNumber(accountNumber), toMoney(request.amount()));
    }

    default WithdrawCommand toWithdrawCommand(String accountNumber, WithdrawRequest request) {
        return new WithdrawCommand(toAccountNumber(accountNumber), toMoney(request.amount()));
    }

    default AuthorizeOverdraftCommand toAuthorizeOverdraftCommand(String accountNumber,
                                                                    AuthorizeOverdraftRequest request) {
        return new AuthorizeOverdraftCommand(toAccountNumber(accountNumber), toMoney(request.overdraftLimit()));
    }

    default AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getAccountNumber().value(),
                account.getBalance().value(),
                account.getOverdraftLimit().value(),
                account.getAccountType().name(),
                account.getDepositCeiling().map(Money::value).orElse(null));
    }

    default AccountStatementResponse toStatementResponse(AccountStatement statement) {
        return new AccountStatementResponse(
                statement.accountNumber().value(),
                statement.accountType().name(),
                statement.balance().value(),
                statement.statementDate(),
                statement.transactions().stream().map(this::toTransactionResponse).toList());
    }

    default TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.type().name(),
                transaction.amount().value(),
                transaction.balanceAfter().value(),
                transaction.occurredAt());
    }

    default AccountNumber toAccountNumber(String value) {
        return new AccountNumber(value);
    }

    default Money toMoney(BigDecimal value) {
        return Money.of(value);
    }

    default AccountType toAccountType(String value) {
        try {
            return AccountType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidAccountTypeException(value);
        }
    }
}
