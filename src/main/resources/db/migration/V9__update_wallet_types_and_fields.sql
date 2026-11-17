-- Normalize wallet_type to E-Wallet types (PERSONAL, MERCHANT, SYSTEM)
-- and remove unused banking-specific columns from wallets.

UPDATE public.wallets
SET wallet_type = 'MERCHANT'
WHERE wallet_type = 'BUSINESS';

UPDATE public.wallets
SET wallet_type = 'PERSONAL'
WHERE wallet_type IN ('CHECKING', 'SAVINGS', 'FOREIGN', 'PAYROLL', 'INVESTMENT', 'JOINT');

ALTER TABLE public.wallets
    DROP CONSTRAINT IF EXISTS wallets_wallet_type_check;

ALTER TABLE public.wallets
    ADD CONSTRAINT wallets_wallet_type_check
        CHECK (wallet_type::text = ANY (ARRAY['PERSONAL'::text, 'MERCHANT'::text, 'SYSTEM'::text]));

ALTER TABLE public.wallets
    DROP COLUMN IF EXISTS interest_rate,
    DROP COLUMN IF EXISTS overdraft_limit,
    DROP COLUMN IF EXISTS minimum_balance,
    DROP COLUMN IF EXISTS branch_code,
    DROP COLUMN IF EXISTS branch_name;
