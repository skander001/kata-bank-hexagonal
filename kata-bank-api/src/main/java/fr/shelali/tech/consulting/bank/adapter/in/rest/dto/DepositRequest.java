package fr.shelali.tech.consulting.bank.adapter.in.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record DepositRequest(
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount
) {
}
