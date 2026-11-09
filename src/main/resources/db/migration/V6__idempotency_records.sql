-- V6__idempotency_records.sql
-- Claim-first idempotency: tách khỏi bảng transactions, scope theo (user, operation, key)

CREATE TABLE IF NOT EXISTS public.idempotency_records (
    id            uuid         NOT NULL,
    user_id       uuid         NOT NULL,
    operation     varchar(32)  NOT NULL,
    idem_key      varchar(64)  NOT NULL,
    request_hash  char(64)     NOT NULL,
    status        varchar(16)  NOT NULL,
    http_status   integer,
    response_body text,
    created_at    timestamptz  NOT NULL,
    updated_at    timestamptz  NOT NULL,
    expires_at    timestamptz  NOT NULL,
    CONSTRAINT idempotency_records_pkey PRIMARY KEY (id),
    CONSTRAINT uk_idem_user_op_key UNIQUE (user_id, operation, idem_key)
);

CREATE INDEX IF NOT EXISTS idx_idem_expires
    ON public.idempotency_records (expires_at);

CREATE INDEX IF NOT EXISTS idx_idem_status_updated
    ON public.idempotency_records (status, updated_at);

-- Cùng key được phép dùng cho operation khác nhau
ALTER TABLE public.transactions
    DROP CONSTRAINT IF EXISTS uk_transaction_user_idempotency;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_transaction_user_type_idem'
    ) THEN
        ALTER TABLE public.transactions
            ADD CONSTRAINT uk_transaction_user_type_idem
            UNIQUE (user_id, type, idempotency_key);
    END IF;
END $$;
