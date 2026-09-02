package fr.shelali.tech.consulting.bank.domain.port.out;

import java.time.Instant;
import java.util.List;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.Transaction;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findByAccountNumberAndOccurredAtBetween(AccountNumber accountNumber, Instant from, Instant to);
}
