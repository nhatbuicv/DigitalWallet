-- V2__wallet_status_history.sql
-- Wallet status audit history table for admin freeze/unfreeze operations

CREATE TABLE IF NOT EXISTS public.wallet_status_history (
    uid uuid NOT NULL,
    wallet_uid uuid NOT NULL,
    previous_status character varying(20) NOT NULL,
    new_status character varying(20) NOT NULL,
    reason character varying(500) NOT NULL,
    changed_by character varying(100) NOT NULL,
    changed_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT wallet_status_history_pkey PRIMARY KEY (uid),
    CONSTRAINT fk_wallet_status_history_wallet FOREIGN KEY (wallet_uid) REFERENCES public.wallets(uid),
    CONSTRAINT wallet_status_history_previous_status_check CHECK (((previous_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'FROZEN'::character varying, 'CLOSED'::character varying, 'PENDING'::character varying, 'SUSPENDED'::character varying])::text[]))),
    CONSTRAINT wallet_status_history_new_status_check CHECK (((new_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'FROZEN'::character varying, 'CLOSED'::character varying, 'PENDING'::character varying, 'SUSPENDED'::character varying])::text[])))
);

CREATE INDEX IF NOT EXISTS idx_wallet_status_history_wallet ON public.wallet_status_history(wallet_uid);
CREATE INDEX IF NOT EXISTS idx_wallet_status_history_changed_at ON public.wallet_status_history(changed_at);