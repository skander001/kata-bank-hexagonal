package fr.shelali.tech.consulting.bank.infrastructure.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.Transaction;
import fr.shelali.tech.consulting.bank.domain.port.out.TransactionRepository;
import fr.shelali.tech.consulting.bank.infrastructure.persistence.mapper.TransactionEntityMapper;
import fr.shelali.tech.consulting.bank.infrastructure.persistence.repository.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;

/**
 * Implementation du port sortant {@link TransactionRepository}, adossee a Spring Data JPA.
 *
 * <p>Plus simple que {@link AccountRepositoryAdapter} : un mouvement est insere une seule fois
 * et jamais mis a jour, donc pas de recherche prealable par cle metier ni de verrouillage
 * optimiste a gerer ici.
 */
@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionEntityMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(jpaRepository.save(mapper.toNewEntity(transaction)));
    }

    @Override
    public List<Transaction> findByAccountNumberAndOccurredAtBetween(AccountNumber accountNumber, Instant from,
                                                                       Instant to) {
        return jpaRepository
                .findByAccountNumberAndOccurredAtBetweenOrderByOccurredAtDesc(accountNumber.value(), from, to)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
