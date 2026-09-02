package fr.shelali.tech.consulting.bank.adapter.in.rest;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.AccountResponse;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.AccountStatementResponse;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.AuthorizeOverdraftRequest;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.CreateAccountRequest;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.DepositRequest;
import fr.shelali.tech.consulting.bank.adapter.in.rest.dto.WithdrawRequest;
import fr.shelali.tech.consulting.bank.adapter.in.rest.mapper.AccountDtoMapper;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountStatement;
import fr.shelali.tech.consulting.bank.domain.port.in.AuthorizeOverdraftUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.CreateAccountUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.DepositUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.GetAccountStatementUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.GetAccountUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.WithdrawUseCase;
import lombok.RequiredArgsConstructor;

/**
 * Adapter primaire REST du compte bancaire. Traduit chaque requete HTTP en appel de port
 * entrant et le resultat du domaine en reponse HTTP : aucune regle metier ici.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final AuthorizeOverdraftUseCase authorizeOverdraftUseCase;
    private final GetAccountStatementUseCase getAccountStatementUseCase;
    private final AccountDtoMapper mapper;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = createAccountUseCase.createAccount(mapper.toCommand(request));
        AccountResponse response = mapper.toResponse(account);
        return ResponseEntity.created(URI.create("/api/v1/accounts/" + response.accountNumber())).body(response);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        Account account = getAccountUseCase.getAccount(new AccountNumber(accountNumber));
        return ResponseEntity.ok(mapper.toResponse(account));
    }

    @PostMapping("/{accountNumber}/deposits")
    public ResponseEntity<AccountResponse> deposit(@PathVariable String accountNumber,
                                                     @Valid @RequestBody DepositRequest request) {
        Account account = depositUseCase.deposit(mapper.toDepositCommand(accountNumber, request));
        return ResponseEntity.ok(mapper.toResponse(account));
    }

    @PostMapping("/{accountNumber}/withdrawals")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable String accountNumber,
                                                      @Valid @RequestBody WithdrawRequest request) {
        Account account = withdrawUseCase.withdraw(mapper.toWithdrawCommand(accountNumber, request));
        return ResponseEntity.ok(mapper.toResponse(account));
    }

    @PutMapping("/{accountNumber}/overdraft")
    public ResponseEntity<AccountResponse> authorizeOverdraft(@PathVariable String accountNumber,
                                                                @Valid @RequestBody AuthorizeOverdraftRequest request) {
        Account account = authorizeOverdraftUseCase.authorizeOverdraft(
                mapper.toAuthorizeOverdraftCommand(accountNumber, request));
        return ResponseEntity.ok(mapper.toResponse(account));
    }

    @GetMapping("/{accountNumber}/statement")
    public ResponseEntity<AccountStatementResponse> getStatement(@PathVariable String accountNumber) {
        AccountStatement statement = getAccountStatementUseCase.getStatement(new AccountNumber(accountNumber));
        return ResponseEntity.ok(mapper.toStatementResponse(statement));
    }
}
