package fr.shelali.tech.consulting.bank.adapter.in.rest.advice;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fr.shelali.tech.consulting.bank.domain.exception.AccountConcurrentUpdateException;
import fr.shelali.tech.consulting.bank.domain.exception.AccountNotFoundException;
import fr.shelali.tech.consulting.bank.domain.exception.DepositCeilingExceededException;
import fr.shelali.tech.consulting.bank.domain.exception.InsufficientBalanceException;
import fr.shelali.tech.consulting.bank.domain.exception.InvalidAccountTypeException;
import fr.shelali.tech.consulting.bank.domain.exception.InvalidAmountException;
import fr.shelali.tech.consulting.bank.domain.exception.OverdraftNotAllowedException;

/**
 * Seul endroit connaissant a la fois les exceptions metier et les codes HTTP : traduit
 * chaque exception du domaine en {@link ProblemDetail} conforme a la RFC 9457.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setType(URI.create("urn:kata-bank:account-not-found"));
        problemDetail.setTitle("Account not found");
        return problemDetail;
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ProblemDetail handleInsufficientBalance(InsufficientBalanceException ex) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create("urn:kata-bank:insufficient-balance"));
        problemDetail.setTitle("Insufficient balance");
        return problemDetail;
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ProblemDetail handleInvalidAmount(InvalidAmountException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("urn:kata-bank:invalid-amount"));
        problemDetail.setTitle("Invalid amount");
        return problemDetail;
    }

    @ExceptionHandler(DepositCeilingExceededException.class)
    public ProblemDetail handleDepositCeilingExceeded(DepositCeilingExceededException ex) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create("urn:kata-bank:deposit-ceiling-exceeded"));
        problemDetail.setTitle("Deposit ceiling exceeded");
        return problemDetail;
    }

    @ExceptionHandler(OverdraftNotAllowedException.class)
    public ProblemDetail handleOverdraftNotAllowed(OverdraftNotAllowedException ex) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create("urn:kata-bank:overdraft-not-allowed"));
        problemDetail.setTitle("Overdraft not allowed");
        return problemDetail;
    }

    @ExceptionHandler(InvalidAccountTypeException.class)
    public ProblemDetail handleInvalidAccountType(InvalidAccountTypeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("urn:kata-bank:invalid-account-type"));
        problemDetail.setTitle("Invalid account type");
        return problemDetail;
    }

    @ExceptionHandler(AccountConcurrentUpdateException.class)
    public ProblemDetail handleAccountConcurrentUpdate(AccountConcurrentUpdateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setType(URI.create("urn:kata-bank:account-concurrent-update"));
        problemDetail.setTitle("Concurrent account update");
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedRequestBody(HttpMessageNotReadableException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Malformed request body");
        problemDetail.setType(URI.create("urn:kata-bank:malformed-request-body"));
        problemDetail.setTitle("Malformed request body");
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationFailure(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed for one or more fields");
        problemDetail.setType(URI.create("urn:kata-bank:validation-error"));
        problemDetail.setTitle("Validation error");
        problemDetail.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedError(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setType(URI.create("urn:kata-bank:internal-error"));
        problemDetail.setTitle("Internal server error");
        return problemDetail;
    }
}
