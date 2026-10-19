-- V4__transaction_idempotency_user_scope.sql
-- Scope idempotency key theo user: thêm user_id, thay unique toàn cục bằng unique (user_id, idempotency_key)

ALTER TABLE public.transactions ADD COLUMN IF NOT EXISTS user_id uuid;

-- ví hệ thống không có user nên giao dịch nạp lấy user từ ví nhận
UPDATE public.transactions t
SET user_id = w.user_uid
FROM public.wallets w
WHERE t.user_id IS NULL AND w.uid = t.from_wallet_id AND w.user_uid IS NOT NULL;

UPDATE public.transactions t
SET user_id = w.user_uid
FROM public.wallets w
WHERE t.user_id IS NULL AND w.uid = t.to_wallet_id AND w.user_uid IS NOT NULL;

ALTER TABLE public.transactions DROP CONSTRAINT IF EXISTS uk_e58eyb3fj0t2cl16q1ll436b7;
ALTER TABLE public.transactions ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE public.transactions ADD CONSTRAINT uk_transaction_user_idempotency UNIQUE (user_id, idempotency_key);

CREATE INDEX IF NOT EXISTS idx_transaction_user ON public.transactions(user_id);
