-- Flyway - ajoute le decouvert autorise sur les comptes.
--
-- Un compte peut se voir accorder une autorisation de decouvert (overdraft_limit >= 0) :
-- un retrait peut alors faire descendre le solde jusqu'a -overdraft_limit inclus. Remplace
-- l'ancienne contrainte "solde toujours >= 0" par une contrainte "solde >= -overdraft_limit",
-- qui redevient equivalente a l'ancienne regle quand overdraft_limit vaut 0 (valeur par
-- defaut : aucun decouvert autorise).

ALTER TABLE accounts
    ADD COLUMN overdraft_limit NUMERIC(19, 2) NOT NULL DEFAULT 0;

ALTER TABLE accounts
    DROP CONSTRAINT ck_accounts_balance_non_negative;

ALTER TABLE accounts
    ADD CONSTRAINT ck_accounts_overdraft_limit_non_negative CHECK (overdraft_limit >= 0),
    ADD CONSTRAINT ck_accounts_balance_within_overdraft CHECK (balance >= -overdraft_limit);
