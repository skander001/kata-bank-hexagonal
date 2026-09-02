package fr.shelali.tech.consulting.bank.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import fr.shelali.tech.consulting.bank.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findByAccountNumberAndOccurredAtBetweenOrderByOccurredAtDesc(
            String accountNumber, Instant from, Instant to);
}
