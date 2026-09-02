package fr.shelali.tech.consulting.bank.domain.port.in;

import fr.shelali.tech.consulting.bank.domain.model.AccountType;
import fr.shelali.tech.consulting.bank.domain.model.Money;

/**
 * @param initialBalance nullable ; le service applicatif l'initialise a zero lorsqu'absent.
 * @param accountType requis.
 * @param depositCeiling nullable ; obligatoire pour {@link AccountType#SAVINGS}, interdit pour
 *                        {@link AccountType#CHECKING} (regle validee par le domaine).
 */
public record CreateAccountCommand(Money initialBalance, AccountType accountType, Money depositCeiling) {
}
