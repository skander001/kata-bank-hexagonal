-- Flyway - migration initiale du schema kata-bank.
--
-- Convention de nommage : V<n>__<description>.sql, une migration par changement,
-- jamais modifiee apres avoir ete appliquee (ajouter une V<n+1> a la place).
--
-- Le schema est volontairement vide : c'est ici que se declarent les tables
-- correspondant aux entites JPA du module kata-bank-db.

CREATE TABLE accounts (
    id             UUID PRIMARY KEY,
    account_number VARCHAR(64)     NOT NULL,
    balance        NUMERIC(19, 2)  NOT NULL,
    version        BIGINT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT uq_accounts_account_number UNIQUE (account_number),
    CONSTRAINT ck_accounts_balance_non_negative CHECK (balance >= 0)
);
