package fr.shelali.tech.consulting.bank.infrastructure.persistence;

import java.util.Optional;

import fr.shelali.tech.consulting.bank.infrastructure.persistence.entity.AccountEntity;
import fr.shelali.tech.consulting.bank.infrastructure.persistence.mapper.AccountEntityMapper;
import fr.shelali.tech.consulting.bank.infrastructure.persistence.repository.AccountJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;


import fr.shelali.tech.consulting.bank.domain.exception.AccountConcurrentUpdateException;
import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.port.out.AccountRepository;
import lombok.RequiredArgsConstructor;

/**
 * Implementation du port sortant {@link AccountRepository}, adossee a Spring Data JPA.
 *
 * <p>Le domaine ignore l'identifiant technique de persistance : sur mise a jour, cet
 * adapter retrouve l'entite existante par numero de compte pour preserver son {@code id}
 * avant d'appliquer les nouvelles valeurs. Le champ {@code version} n'est jamais reporte
 * manuellement (voir {@link AccountEntityMapper#updateEntity}) : il reste sous le controle
 * exclusif d'Hibernate.
 *
 * <p>Correction du verrouillage optimiste : pour qu'un retrait/depot concurrent soit
 * correctement rejete, la lecture initiale du compte (dans le service applicatif) et cet
 * appel a {@code save} doivent s'executer dans la meme transaction / le meme contexte de
 * persistance, de sorte que l'entite retrouvee ici soit l'instance managee deja chargee
 * plus tot dans le cas d'usage et que le controle de version d'Hibernate porte sur sa
 * propre version suivie, pas sur une valeur reconstituee. Cette frontiere transactionnelle
 * est fournie par les decorateurs de {@code kata-bank-adapter.infrastructure.transaction}
 * qui enveloppent chaque cas d'usage mutateur.
 */
@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountEntityMapper mapper;

    @Override
    public Account save(Account account) {
        try {
            AccountEntity entity = jpaRepository.findByAccountNumber(account.getAccountNumber().value())
                    .map(existing -> {
                        mapper.updateEntity(account, existing);
                        return existing;
                    })
                    .orElseGet(() -> mapper.toNewEntity(account));
            AccountEntity saved = jpaRepository.save(entity);
            return mapper.toDomain(saved);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new AccountConcurrentUpdateException(account.getAccountNumber());
        } catch (DataIntegrityViolationException e) {
            // Course de creation concurrente sur le meme numero de compte (contrainte
            // d'unicite account_number) : traitee comme un conflit de mise a jour
            // concurrente, la semantique metier la plus proche disponible dans le domaine.
            throw new AccountConcurrentUpdateException(account.getAccountNumber());
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(AccountNumber accountNumber) {
        return jpaRepository.findByAccountNumber(accountNumber.value())
                .map(mapper::toDomain);
    }
}
