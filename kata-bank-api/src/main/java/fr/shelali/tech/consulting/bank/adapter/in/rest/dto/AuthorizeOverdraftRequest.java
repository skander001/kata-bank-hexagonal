package fr.shelali.tech.consulting.bank.adapter.in.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AuthorizeOverdraftRequest(
        @NotNull @DecimalMin(value = "0.00") BigDecimal overdraftLimit
) {
}
