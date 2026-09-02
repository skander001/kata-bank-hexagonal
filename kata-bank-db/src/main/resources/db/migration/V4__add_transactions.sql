-- Flyway - ajoute l'historique des mouvements (depots/retraits) par compte.
--
-- Chaque depot/retrait via les cas d'usage existants enregistre desormais un mouvement,
-- utilise pour construire le releve de compte du mois glissant. Rattache au compte par
-- account_number (cle metier), comme le fait deja tout le domaine, et non par le id
-- technique de accounts que le domaine ignore.

CREATE TABLE transactions (
    id             UUID PRIMARY KEY,
    account_number VARCHAR(64)     NOT NULL,
    type           VARCHAR(20)     NOT NULL,
    amount         NUMERIC(19, 2)  NOT NULL,
    balance_after  NUMERIC(19, 2)  NOT NULL,
    occurred_at    TIMESTAMPTZ     NOT NULL,
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT ck_transactions_type CHECK (type IN ('DEPOSIT', 'WITHDRAWAL')),
    CONSTRAINT ck_transactions_amount_positive CHECK (amount > 0)
);

CREATE INDEX ix_transactions_account_number_occurred_at
    ON transactions (account_number, occurred_at DESC);
