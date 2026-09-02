package fr.shelali.tech.consulting.bank.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entite JPA du compte bancaire. Modele de persistance, distinct de l'agregat du domaine
 * {@code Account} : porte un identifiant technique {@code id} que le domaine ignore, et le
 * verrouillage optimiste JPA ({@code @Version}).
 */
@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountEntity {

    @Id
    private UUID id;

    @Setter
    @Column(name = "account_number", nullable = false, unique = true, length = 64)
    private String accountNumber;

    @Setter
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Setter
    @Column(name = "overdraft_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal overdraftLimit;

    @Setter
    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @Setter
    @Column(name = "deposit_ceiling", precision = 19, scale = 2)
    private BigDecimal depositCeiling;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AccountEntity(UUID id, String accountNumber, BigDecimal balance, BigDecimal overdraftLimit,
                          String accountType, BigDecimal depositCeiling, long version) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.overdraftLimit = overdraftLimit;
        this.accountType = accountType;
        this.depositCeiling = depositCeiling;
        this.version = version;
    }
}
