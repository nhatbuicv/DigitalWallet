-- V5__system_wallet_limits_nullable.sql
-- The SYSTEM wallet is an intermediary/system account and does not carry
-- transaction limits. Allow the limit columns to be NULL and clear the
-- limits that were assigned to the SYSTEM wallet by mistake.
-- Customer wallets still receive their limits from WalletLimitProperties
-- when they are created.

ALTER TABLE public.wallets
    ALTER COLUMN single_transaction_limit DROP NOT NULL;

UPDATE public.wallets
SET single_transaction_limit = NULL,
    daily_limit = NULL,
    monthly_limit = NULL
WHERE wallet_type = 'SYSTEM';
