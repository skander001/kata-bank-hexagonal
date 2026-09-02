package fr.shelali.tech.consulting.bank.adapter.in.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.shelali.tech.consulting.bank.adapter.in.rest.mapper.AccountDtoMapperImpl;
import fr.shelali.tech.consulting.bank.domain.exception.AccountConcurrentUpdateException;
import fr.shelali.tech.consulting.bank.domain.exception.AccountNotFoundException;
import fr.shelali.tech.consulting.bank.domain.exception.InsufficientBalanceException;
import fr.shelali.tech.consulting.bank.domain.exception.InvalidAmountException;
import fr.shelali.tech.consulting.bank.domain.exception.OverdraftNotAllowedException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountStatement;
import fr.shelali.tech.consulting.bank.domain.model.AccountType;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;
import fr.shelali.tech.consulting.bank.domain.model.Transaction;
import fr.shelali.tech.consulting.bank.domain.port.in.AuthorizeOverdraftUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.CreateAccountUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.DepositUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.GetAccountStatementUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.GetAccountUseCase;
import fr.shelali.tech.consulting.bank.domain.port.in.WithdrawUseCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountController.class)
@Import(AccountDtoMapperImpl.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateAccountUseCase createAccountUseCase;

    @MockitoBean
    private GetAccountUseCase getAccountUseCase;

    @MockitoBean
    private DepositUseCase depositUseCase;

    @MockitoBean
    private WithdrawUseCase withdrawUseCase;

    @MockitoBean
    private AuthorizeOverdraftUseCase authorizeOverdraftUseCase;

    @MockitoBean
    private GetAccountStatementUseCase getAccountStatementUseCase;

    private static Account anAccount(String number, String balance) {
        return anAccount(number, balance, "0.00");
    }

    private static Account anAccount(String number, String balance, String overdraftLimit) {
        return Account.reconstitute(
                new AccountNumber(number), Balance.of(new BigDecimal(balance)), 0L,
                Money.of(new BigDecimal(overdraftLimit)), AccountType.CHECKING, null);
    }

    private static Account aSavingsAccount(String number, String balance, String depositCeiling) {
        return Account.reconstitute(
                new AccountNumber(number), Balance.of(new BigDecimal(balance)), 0L,
                Money.zero(), AccountType.SAVINGS, Money.of(new BigDecimal(depositCeiling)));
    }

    @Nested
    class Create {

        @Test
        void happy_path_returns_201_with_location_header_and_body() throws Exception {
            Account created = anAccount("ACC-1", "100.00");
            when(createAccountUseCase.createAccount(any())).thenReturn(created);

            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"initialBalance\": 100.00, \"accountType\": \"CHECKING\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/v1/accounts/ACC-1"))
                    .andExpect(jsonPath("$.accountNumber").value("ACC-1"))
                    .andExpect(jsonPath("$.balance").value(100.00))
                    .andExpect(jsonPath("$.accountType").value("CHECKING"));
        }

        @Test
        void happy_path_with_no_initial_balance_returns_201() throws Exception {
            Account created = anAccount("ACC-2", "0.00");
            when(createAccountUseCase.createAccount(any())).thenReturn(created);

            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"accountType\": \"CHECKING\"}"))
                    .andExpect(status().isCreated());
        }

        @Test
        void invalid_initial_balance_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"initialBalance\": -10.00, \"accountType\": \"CHECKING\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:validation-error"));
        }

        @Test
        void savings_happy_path_returns_201_with_account_type_and_deposit_ceiling() throws Exception {
            Account created = aSavingsAccount("ACC-3", "500.00", "1000.00");
            when(createAccountUseCase.createAccount(any())).thenReturn(created);

            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"initialBalance\": 500.00, \"accountType\": \"SAVINGS\", "
                                    + "\"depositCeiling\": 1000.00}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                    .andExpect(jsonPath("$.depositCeiling").value(1000.00));
        }

        @Test
        void missing_account_type_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:validation-error"));
        }

        @Test
        void blank_account_type_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"accountType\": \"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:validation-error"));
        }

        @Test
        void unknown_account_type_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"accountType\": \"BUSINESS\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:invalid-account-type"));
        }

        @Test
        void savings_without_ceiling_returns_400() throws Exception {
            // The SAVINGS-requires-ceiling rule is enforced by the domain (Account.open), which
            // this slice test doesn't exercise directly since createAccountUseCase is mocked:
            // stub it to surface the same InvalidAmountException the real use case would throw.
            when(createAccountUseCase.createAccount(any()))
                    .thenThrow(new InvalidAmountException("Deposit ceiling is required for a savings account"));

            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"accountType\": \"SAVINGS\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:invalid-amount"));
        }
    }

    @Nested
    class Get {

        @Test
        void happy_path_returns_200() throws Exception {
            Account account = anAccount("ACC-1", "42.50");
            when(getAccountUseCase.getAccount(new AccountNumber("ACC-1"))).thenReturn(account);

            mockMvc.perform(get("/api/v1/accounts/ACC-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountNumber").value("ACC-1"))
                    .andExpect(jsonPath("$.balance").value(42.50));
        }

        @Test
        void not_found_returns_404_problem_detail() throws Exception {
            AccountNumber accountNumber = new AccountNumber("UNKNOWN");
            when(getAccountUseCase.getAccount(accountNumber))
                    .thenThrow(new AccountNotFoundException(accountNumber));

            mockMvc.perform(get("/api/v1/accounts/UNKNOWN"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:account-not-found"))
                    .andExpect(jsonPath("$.detail").exists());
        }
    }

    @Nested
    class Deposit {

        @Test
        void happy_path_returns_200() throws Exception {
            Account updated = anAccount("ACC-1", "150.00");
            when(depositUseCase.deposit(any())).thenReturn(updated);

            mockMvc.perform(post("/api/v1/accounts/ACC-1/deposits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 50.00}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(150.00));
        }

        @Test
        void zero_amount_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/accounts/ACC-1/deposits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 0}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:validation-error"));
        }

        @Test
        void negative_amount_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/accounts/ACC-1/deposits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": -5.00}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void malformed_json_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/accounts/ACC-1/deposits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{not-json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void not_found_returns_404() throws Exception {
            when(depositUseCase.deposit(any()))
                    .thenThrow(new AccountNotFoundException(new AccountNumber("ACC-1")));

            mockMvc.perform(post("/api/v1/accounts/ACC-1/deposits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 50.00}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Withdraw {

        @Test
        void happy_path_returns_200() throws Exception {
            Account updated = anAccount("ACC-1", "50.00");
            when(withdrawUseCase.withdraw(any())).thenReturn(updated);

            mockMvc.perform(post("/api/v1/accounts/ACC-1/withdrawals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 50.00}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(50.00));
        }

        @Test
        void insufficient_balance_returns_422() throws Exception {
            AccountNumber accountNumber = new AccountNumber("ACC-1");
            when(withdrawUseCase.withdraw(any()))
                    .thenThrow(new InsufficientBalanceException(
                            accountNumber, Balance.of(new BigDecimal("10.00")), Money.of(new BigDecimal("50.00")),
                            Money.zero()));

            mockMvc.perform(post("/api/v1/accounts/ACC-1/withdrawals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 50.00}"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:insufficient-balance"));
        }

        @Test
        void withdrawal_covered_by_overdraft_returns_200_with_negative_balance() throws Exception {
            Account updated = anAccount("ACC-1", "-20.00", "100.00");
            when(withdrawUseCase.withdraw(any())).thenReturn(updated);

            mockMvc.perform(post("/api/v1/accounts/ACC-1/withdrawals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 50.00}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(-20.00))
                    .andExpect(jsonPath("$.overdraftLimit").value(100.00));
        }

        @Test
        void not_found_returns_404() throws Exception {
            when(withdrawUseCase.withdraw(any()))
                    .thenThrow(new AccountNotFoundException(new AccountNumber("ACC-1")));

            mockMvc.perform(post("/api/v1/accounts/ACC-1/withdrawals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 50.00}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void concurrent_update_returns_409() throws Exception {
            when(withdrawUseCase.withdraw(any()))
                    .thenThrow(new AccountConcurrentUpdateException(new AccountNumber("ACC-1")));

            mockMvc.perform(post("/api/v1/accounts/ACC-1/withdrawals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 50.00}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:account-concurrent-update"));
        }

        @Test
        void zero_amount_returns_400() throws Exception {
            mockMvc.perform(post("/api/v1/accounts/ACC-1/withdrawals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 0}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class AuthorizeOverdraft {

        @Test
        void happy_path_returns_200() throws Exception {
            Account updated = anAccount("ACC-1", "10.00", "500.00");
            when(authorizeOverdraftUseCase.authorizeOverdraft(any())).thenReturn(updated);

            mockMvc.perform(put("/api/v1/accounts/ACC-1/overdraft")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"overdraftLimit\": 500.00}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.overdraftLimit").value(500.00));
        }

        @Test
        void negative_limit_returns_400() throws Exception {
            mockMvc.perform(put("/api/v1/accounts/ACC-1/overdraft")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"overdraftLimit\": -1.00}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:validation-error"));
        }

        @Test
        void missing_limit_returns_400() throws Exception {
            mockMvc.perform(put("/api/v1/accounts/ACC-1/overdraft")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:validation-error"));
        }

        @Test
        void not_found_returns_404() throws Exception {
            when(authorizeOverdraftUseCase.authorizeOverdraft(any()))
                    .thenThrow(new AccountNotFoundException(new AccountNumber("ACC-1")));

            mockMvc.perform(put("/api/v1/accounts/ACC-1/overdraft")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"overdraftLimit\": 500.00}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void savings_account_returns_422() throws Exception {
            when(authorizeOverdraftUseCase.authorizeOverdraft(any()))
                    .thenThrow(new OverdraftNotAllowedException(new AccountNumber("ACC-1")));

            mockMvc.perform(put("/api/v1/accounts/ACC-1/overdraft")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"overdraftLimit\": 0}"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:overdraft-not-allowed"));
        }
    }

    @Nested
    class GetStatement {

        @Test
        void happy_path_returns_200_with_transactions_in_reverse_chronological_order() throws Exception {
            AccountNumber accountNumber = new AccountNumber("ACC-1");
            Instant statementDate = Instant.parse("2026-08-31T10:00:00Z");
            Transaction older = Transaction.deposit(accountNumber, Money.of(new BigDecimal("50.00")),
                    Balance.of(new BigDecimal("150.00")), Instant.parse("2026-08-10T00:00:00Z"));
            Transaction newer = Transaction.withdrawal(accountNumber, Money.of(new BigDecimal("30.00")),
                    Balance.of(new BigDecimal("120.00")), Instant.parse("2026-08-20T00:00:00Z"));
            AccountStatement statement = new AccountStatement(
                    accountNumber, AccountType.CHECKING, Balance.of(new BigDecimal("120.00")), statementDate,
                    List.of(newer, older));
            when(getAccountStatementUseCase.getStatement(accountNumber)).thenReturn(statement);

            mockMvc.perform(get("/api/v1/accounts/ACC-1/statement"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountNumber").value("ACC-1"))
                    .andExpect(jsonPath("$.accountType").value("CHECKING"))
                    .andExpect(jsonPath("$.balance").value(120.00))
                    .andExpect(jsonPath("$.transactions").isArray())
                    .andExpect(jsonPath("$.transactions[0].type").value("WITHDRAWAL"))
                    .andExpect(jsonPath("$.transactions[0].amount").value(30.00))
                    .andExpect(jsonPath("$.transactions[1].type").value("DEPOSIT"))
                    .andExpect(jsonPath("$.transactions[1].amount").value(50.00));
        }

        @Test
        void not_found_returns_404() throws Exception {
            when(getAccountStatementUseCase.getStatement(new AccountNumber("UNKNOWN")))
                    .thenThrow(new AccountNotFoundException(new AccountNumber("UNKNOWN")));

            mockMvc.perform(get("/api/v1/accounts/UNKNOWN/statement"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:account-not-found"));
        }
    }

    @Nested
    class UnexpectedErrors {

        @Test
        void generic_exception_returns_500_without_leaking_message() throws Exception {
            when(getAccountUseCase.getAccount(eq(new AccountNumber("ACC-1"))))
                    .thenThrow(new RuntimeException("db password is hunter2"));

            mockMvc.perform(get("/api/v1/accounts/ACC-1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.type").value("urn:kata-bank:internal-error"))
                    .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                    .andExpect(content().string(org.hamcrest.Matchers.not(
                            org.hamcrest.Matchers.containsString("hunter2"))));
        }
    }
}
