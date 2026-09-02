package fr.shelali.tech.consulting.bank.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant occurredAt
) {
}
