package fr.shelali.tech.consulting.bank.adapter.in.rest.dto;

import java.math.BigDecimal;

public record AccountResponse(
        String accountNumber,
        BigDecimal balance,
        BigDecimal overdraftLimit,
        String accountType,
        BigDecimal depositCeiling
) {
}
