ALTER TABLE public.wallets
    ALTER COLUMN single_transaction_limit DROP NOT NULL;

UPDATE public.wallets
SET single_transaction_limit = NULL,
    daily_limit = NULL,
    monthly_limit = NULL
WHERE wallet_type = 'SYSTEM';
