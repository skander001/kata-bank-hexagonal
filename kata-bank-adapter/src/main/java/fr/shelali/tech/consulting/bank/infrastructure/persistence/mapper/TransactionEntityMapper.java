package fr.shelali.tech.consulting.bank.infrastructure.persistence.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;
import fr.shelali.tech.consulting.bank.domain.model.Transaction;
import fr.shelali.tech.consulting.bank.domain.model.TransactionType;
import fr.shelali.tech.consulting.bank.infrastructure.persistence.entity.TransactionEntity;

/**
 * Traduction entite JPA &lt;-&gt; {@link Transaction} du domaine.
 *
 * <p>{@code Transaction} est un record immuable sans setters exposes sur son pendant JPA : la
 * generation MapStruct par introspection ne s'y prete pas, ces conversions sont donc ecrites
 * explicitement, a l'image de {@link AccountEntityMapper}.
 */
@Mapper
public interface TransactionEntityMapper {

    default Transaction toDomain(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Transaction(
                entity.getId(),
                new AccountNumber(entity.getAccountNumber()),
                toTransactionType(entity.getType()),
                Money.of(entity.getAmount()),
                Balance.of(entity.getBalanceAfter()),
                entity.getOccurredAt());
    }

    default TransactionEntity toNewEntity(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new TransactionEntity(
                transaction.id(),
                transaction.accountNumber().value(),
                toTransactionTypeValue(transaction.type()),
                transaction.amount().value(),
                transaction.balanceAfter().value(),
                transaction.occurredAt());
    }

    /**
     * {@code TransactionType.valueOf} est sans risque ici : la colonne n'est jamais ecrite que
     * par {@link #toTransactionTypeValue}, donc toujours une des deux valeurs de l'enum.
     */
    default TransactionType toTransactionType(String value) {
        return value == null ? null : TransactionType.valueOf(value);
    }

    default String toTransactionTypeValue(TransactionType type) {
        return type == null ? null : type.name();
    }

    default BigDecimal toBigDecimal(Money money) {
        return money == null ? null : money.value();
    }
}
