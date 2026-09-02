package fr.shelali.tech.consulting.bank.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entite JPA d'un mouvement de compte. Modele de persistance, distinct du domaine
 * {@code Transaction} : porte un identifiant technique {@code id} (ici identique a celui du
 * domaine, genere en amont) et un horodatage technique {@code createdAt} que le domaine ignore.
 *
 * <p>A la difference de {@link AccountEntity}, aucun setter : un mouvement est un fait insere
 * une seule fois, jamais mis a jour.
 */
@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(name = "account_number", nullable = false, length = 64)
    private String accountNumber;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TransactionEntity(UUID id, String accountNumber, String type, BigDecimal amount,
                              BigDecimal balanceAfter, Instant occurredAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.occurredAt = occurredAt;
    }
}
