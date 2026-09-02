package fr.shelali.tech.consulting.bank.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import fr.shelali.tech.consulting.bank.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);
}
