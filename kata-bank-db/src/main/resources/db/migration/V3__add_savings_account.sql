-- Flyway - ajoute le compte epargne (savings) et son plafond de depot.
--
-- Un compte a desormais un type (CHECKING/SAVINGS). Un compte epargne porte un plafond de
-- depot obligatoire (deposit_ceiling) et ne peut jamais avoir de decouvert autorise. Les lignes
-- existantes basculent en CHECKING sans plafond, coherent avec le comportement actuel.

ALTER TABLE accounts
    ADD COLUMN account_type VARCHAR(20) NOT NULL DEFAULT 'CHECKING',
    ADD COLUMN deposit_ceiling NUMERIC(19, 2) NULL;

ALTER TABLE accounts
    ADD CONSTRAINT ck_accounts_account_type CHECK (account_type IN ('CHECKING', 'SAVINGS')),
    ADD CONSTRAINT ck_accounts_savings_ceiling_required
        CHECK (account_type <> 'SAVINGS' OR deposit_ceiling IS NOT NULL),
    ADD CONSTRAINT ck_accounts_checking_no_ceiling
        CHECK (account_type = 'SAVINGS' OR deposit_ceiling IS NULL),
    ADD CONSTRAINT ck_accounts_balance_within_ceiling
        CHECK (deposit_ceiling IS NULL OR balance <= deposit_ceiling),
    ADD CONSTRAINT ck_accounts_savings_no_overdraft
        CHECK (account_type <> 'SAVINGS' OR overdraft_limit = 0);
