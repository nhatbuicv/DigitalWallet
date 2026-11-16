-- V8__admin_transaction_search_indexes.sql
-- Composite & Trigram GIN indexes phục vụ Admin Transaction Search, Sorting & User Ledger History

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 1. Transactions: Composite & Default Sort Indexes
CREATE INDEX IF NOT EXISTS idx_tx_created_at
    ON public.transactions (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_tx_status_created
    ON public.transactions (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_tx_type_created
    ON public.transactions (type, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_tx_status_type_created
    ON public.transactions (status, type, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_tx_from_wallet_created
    ON public.transactions (from_wallet_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_tx_to_wallet_created
    ON public.transactions (to_wallet_id, created_at DESC);

-- 2. Transactions: GIN Trigram Index cho tìm kiếm mờ LIKE '%...%' trên reference_code
CREATE INDEX IF NOT EXISTS idx_tx_reference_code_trgm
    ON public.transactions USING gin (lower(reference_code) gin_trgm_ops);

-- 3. Ledger Entries: Composite Indexes cho lịch sử ví user & chi tiết giao dịch
CREATE INDEX IF NOT EXISTS idx_ledger_wallet_created
    ON public.ledger_entries (wallet_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ledger_wallet_type_created
    ON public.ledger_entries (wallet_id, entry_type, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ledger_tx_created
    ON public.ledger_entries (transaction_id, created_at ASC);
