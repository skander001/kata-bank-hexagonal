package fr.shelali.tech.consulting.bank.adapter.in.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

/**
 * @param initialBalance optionnel ; l'absence vaut solde initial nul.
 * @param accountType requis : {@code CHECKING} ou {@code SAVINGS}.
 * @param depositCeiling optionnel ; obligatoire pour un compte {@code SAVINGS}, interdit pour un
 *                        compte {@code CHECKING} (regle validee par le domaine).
 */
public record CreateAccountRequest(
        @DecimalMin(value = "0.00") BigDecimal initialBalance,
        @NotBlank String accountType,
        @DecimalMin(value = "0.00") BigDecimal depositCeiling
) {
}
