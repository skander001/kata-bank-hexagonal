package fr.shelali.tech.consulting.bank.infrastructure.persistence.mapper;

import java.math.BigDecimal;
import java.util.UUID;

import fr.shelali.tech.consulting.bank.infrastructure.persistence.entity.AccountEntity;
import org.mapstruct.Mapper;

import fr.shelali.tech.consulting.bank.domain.model.Account;
import fr.shelali.tech.consulting.bank.domain.model.AccountNumber;
import fr.shelali.tech.consulting.bank.domain.model.AccountType;
import fr.shelali.tech.consulting.bank.domain.model.Balance;
import fr.shelali.tech.consulting.bank.domain.model.Money;

/**
 * Traduction entite JPA &lt;-&gt; agregat du domaine.
 *
 * <p>{@link Account} est immuable et ne s'instancie que via ses fabriques statiques
 * ({@code open}, {@code reconstitute}) : la generation MapStruct par introspection de
 * constructeur ne s'y prete pas, ces conversions sont donc ecrites explicitement. Les value
 * objects {@link AccountNumber}, {@link Balance} et {@link Money} ne sont pas non plus
 * auto-mappables par MapStruct : des methodes de conversion dediees sont fournies.
 */
@Mapper
public interface AccountEntityMapper {

    default Account toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }
        return Account.reconstitute(
                toAccountNumber(entity.getAccountNumber()),
                toBalance(entity.getBalance()),
                entity.getVersion(),
                toMoney(entity.getOverdraftLimit()),
                toAccountType(entity.getAccountType()),
                toMoney(entity.getDepositCeiling()));
    }

    /**
     * Construit une nouvelle entite (nouvel identifiant technique) a partir d'un compte
     * du domaine. Reserve a la creation ; pour une mise a jour, voir {@link #updateEntity}.
     */
    default AccountEntity toNewEntity(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountEntity(
                UUID.randomUUID(),
                toAccountNumberValue(account.getAccountNumber()),
                toBigDecimal(account.getBalance()),
                toBigDecimal(account.getOverdraftLimit()),
                toAccountTypeValue(account.getAccountType()),
                account.getDepositCeiling().map(Money::value).orElse(null),
                account.getVersion());
    }

    /**
     * Reporte l'etat du compte du domaine sur une entite existante, en preservant son
     * identifiant technique.
     *
     * <p>Ne touche jamais a {@code version} : ce champ est gere exclusivement par Hibernate
     * (verrouillage optimiste JPA). Tant que la sequence find -> decide -> save d'un cas
     * d'usage s'execute dans une seule transaction (voir les decorateurs transactionnels de
     * kata-bank-adapter), l'entite retrouvee ici est l'instance managee deja chargee dans le
     * meme contexte de persistance : c'est la version qu'Hibernate suit lui-meme qui sert de
     * base a la comparaison optimiste, pas une valeur reconstituee manuellement.
     */
    default void updateEntity(Account account, AccountEntity entity) {
        entity.setAccountNumber(toAccountNumberValue(account.getAccountNumber()));
        entity.setBalance(toBigDecimal(account.getBalance()));
        entity.setOverdraftLimit(toBigDecimal(account.getOverdraftLimit()));
        entity.setAccountType(toAccountTypeValue(account.getAccountType()));
        entity.setDepositCeiling(account.getDepositCeiling().map(Money::value).orElse(null));
    }

    default AccountNumber toAccountNumber(String value) {
        return value == null ? null : new AccountNumber(value);
    }

    default String toAccountNumberValue(AccountNumber accountNumber) {
        return accountNumber == null ? null : accountNumber.value();
    }

    default Balance toBalance(BigDecimal value) {
        return value == null ? null : Balance.of(value);
    }

    default BigDecimal toBigDecimal(Balance balance) {
        return balance == null ? null : balance.value();
    }

    default Money toMoney(BigDecimal value) {
        return value == null ? null : Money.of(value);
    }

    default BigDecimal toBigDecimal(Money money) {
        return money == null ? null : money.value();
    }

    /**
     * {@code AccountType.valueOf} est sans risque ici : la colonne n'est jamais ecrite que par
     * {@link #toAccountTypeValue}, donc toujours une des deux valeurs de l'enum.
     */
    default AccountType toAccountType(String value) {
        return value == null ? null : AccountType.valueOf(value);
    }

    default String toAccountTypeValue(AccountType accountType) {
        return accountType == null ? null : accountType.name();
    }
}
