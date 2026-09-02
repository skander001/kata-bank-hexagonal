package fr.shelali.tech.consulting.bank.domain.port.in;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.Money;

public record AuthorizeOverdraftCommand(AccountNumber accountNumber, Money overdraftLimit) {
}
