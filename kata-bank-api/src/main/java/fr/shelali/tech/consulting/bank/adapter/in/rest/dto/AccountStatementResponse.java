package fr.shelali.tech.consulting.bank.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AccountStatementResponse(
        String accountNumber,
        String accountType,
        BigDecimal balance,
        Instant statementDate,
        List<TransactionResponse> transactions
) {
}
