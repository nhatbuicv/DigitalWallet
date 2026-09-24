-- V1__init_schema.sql
-- Initial database schema for Digital Wallet
-- Generated from existing database

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- =============================================
-- SCHEMA
-- =============================================
CREATE SCHEMA IF NOT EXISTS public;

-- =============================================
-- SEQUENCES
-- =============================================
CREATE SEQUENCE IF NOT EXISTS public.customer_id_seq
    START WITH 100000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS public.wallet_serial_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- =============================================
-- TABLE: provinces
-- =============================================
CREATE TABLE IF NOT EXISTS public.provinces (
    code character varying(3) NOT NULL,
    name character varying(255) NOT NULL,
    CONSTRAINT provinces_pkey PRIMARY KEY (code),
    CONSTRAINT uk_l256wnwfdobq071mcq7rckr9y UNIQUE (name)
    );

-- =============================================
-- TABLE: users
-- =============================================
CREATE TABLE IF NOT EXISTS public.users (
    uid uuid NOT NULL,
    address text,
    created_at timestamp(6) with time zone,
    created_by character varying(255),
    customer_id character varying(50) NOT NULL,
    date_of_birth date,
    email character varying(100) NOT NULL,
    failed_login_attempts integer,
    full_name character varying(100) NOT NULL,
    last_login timestamp(6) with time zone,
    lock_time timestamp(6) with time zone,
    password character varying(255) NOT NULL,
    phone character varying(15),
    phone_verified boolean,
    role character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    token_version integer,
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    wallet_non_locked boolean,
    province_code character varying(3),
    CONSTRAINT users_pkey PRIMARY KEY (uid),
    CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email),
    CONSTRAINT uk_72efjd5u3nqtjdduip5wwvd39 UNIQUE (customer_id),
    CONSTRAINT uk_du5v5sr43g5bfnji4vb8hg5s3 UNIQUE (phone),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'USER'::character varying, 'STAFF'::character varying, 'MANAGER'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'LOCKED'::character varying, 'PENDING'::character varying, 'SUSPENDED'::character varying])::text[]))),
    CONSTRAINT fk_user_province FOREIGN KEY (province_code) REFERENCES public.provinces(code)
    );

CREATE INDEX IF NOT EXISTS idx_user_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_user_phone ON public.users(phone);
CREATE INDEX IF NOT EXISTS idx_user_customer_id ON public.users(customer_id);

-- =============================================
-- TABLE: wallets
-- =============================================
CREATE TABLE IF NOT EXISTS public.wallets (
    uid uuid NOT NULL,
    available_balance bigint NOT NULL,
    balance bigint NOT NULL,
    blocked_balance bigint,
    branch_code character varying(10),
    branch_name character varying(100),
    closed_at timestamp(6) with time zone,
                               closed_reason character varying(255),
    created_at timestamp(6) with time zone,
                               created_by character varying(255),
    currency character varying(3) NOT NULL,
    daily_limit bigint,
    interest_rate bigint,
    minimum_balance bigint,
    monthly_limit bigint,
    opened_at timestamp(6) with time zone,
    overdraft_limit bigint,
    single_transaction_limit bigint NOT NULL,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone,
    updated_by character varying(255),
    version integer,
    wallet_name character varying(100) NOT NULL,
    wallet_number character varying(12) NOT NULL,
    wallet_type character varying(255) NOT NULL,
    user_uid uuid,
    CONSTRAINT wallets_pkey PRIMARY KEY (uid),
    CONSTRAINT uk_6d820xgaueq4iuukwtmuygb4j UNIQUE (wallet_number),
    CONSTRAINT wallets_currency_check CHECK (((currency)::text = ANY ((ARRAY['VND'::character varying, 'USD'::character varying, 'EUR'::character varying])::text[]))),
    CONSTRAINT wallets_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'FROZEN'::character varying, 'CLOSED'::character varying, 'PENDING'::character varying, 'SUSPENDED'::character varying])::text[]))),
    CONSTRAINT wallets_wallet_type_check CHECK (((wallet_type)::text = ANY ((ARRAY['CHECKING'::character varying, 'SAVINGS'::character varying, 'BUSINESS'::character varying, 'FOREIGN'::character varying, 'PAYROLL'::character varying, 'INVESTMENT'::character varying, 'JOINT'::character varying, 'SYSTEM'::character varying])::text[]))),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_uid) REFERENCES public.users(uid)
    );

CREATE INDEX IF NOT EXISTS idx_wallet_user_uid ON public.wallets(user_uid);
CREATE INDEX IF NOT EXISTS idx_wallet_wallet_number ON public.wallets(wallet_number);
CREATE INDEX IF NOT EXISTS idx_wallet_status ON public.wallets(status);

-- =============================================
-- TABLE: transactions
-- =============================================
CREATE TABLE IF NOT EXISTS public.transactions (
    id uuid NOT NULL,
    amount bigint NOT NULL,
    completed_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    currency character varying(3) NOT NULL,
    description character varying(255),
    idempotency_key character varying(64) NOT NULL,
    reference_code character varying(32) NOT NULL,
    status character varying(20) NOT NULL,
    type character varying(20) NOT NULL,
    from_wallet_id uuid,
    to_wallet_id uuid,
    CONSTRAINT transactions_pkey PRIMARY KEY (id),
    CONSTRAINT uk_e58eyb3fj0t2cl16q1ll436b7 UNIQUE (idempotency_key),
    CONSTRAINT uk_q0y5v03ic3j1v5tyx1dt1ufjs UNIQUE (reference_code),
    CONSTRAINT transactions_currency_check CHECK (((currency)::text = ANY ((ARRAY['VND'::character varying, 'USD'::character varying, 'EUR'::character varying])::text[]))),
    CONSTRAINT transactions_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'REVERSED'::character varying])::text[]))),
    CONSTRAINT transactions_type_check CHECK (((type)::text = ANY ((ARRAY['DEPOSIT'::character varying, 'WITHDRAW'::character varying, 'TRANSFER'::character varying, 'PAYMENT'::character varying, 'REFUND'::character varying])::text[]))),
    CONSTRAINT fk_transaction_from_wallet FOREIGN KEY (from_wallet_id) REFERENCES public.wallets(uid),
    CONSTRAINT fk_transaction_to_wallet FOREIGN KEY (to_wallet_id) REFERENCES public.wallets(uid)
    );

CREATE INDEX IF NOT EXISTS idx_transaction_from_wallet ON public.transactions(from_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transaction_to_wallet ON public.transactions(to_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transaction_reference_code ON public.transactions(reference_code);
CREATE INDEX IF NOT EXISTS idx_transaction_status ON public.transactions(status);

-- =============================================
-- TABLE: ledger_entries
-- =============================================
CREATE TABLE IF NOT EXISTS public.ledger_entries (
    id uuid NOT NULL,
    amount bigint NOT NULL,
    balance_after bigint NOT NULL,
    balance_before bigint NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    entry_type character varying(10) NOT NULL,
    transaction_id uuid NOT NULL,
    wallet_id uuid NOT NULL,
    CONSTRAINT ledger_entries_pkey PRIMARY KEY (id),
    CONSTRAINT ledger_entries_entry_type_check CHECK (((entry_type)::text = ANY ((ARRAY['DEBIT'::character varying, 'CREDIT'::character varying])::text[]))),
    CONSTRAINT fk_ledger_transaction FOREIGN KEY (transaction_id) REFERENCES public.transactions(id),
    CONSTRAINT fk_ledger_wallet FOREIGN KEY (wallet_id) REFERENCES public.wallets(uid)
    );

CREATE INDEX IF NOT EXISTS idx_ledger_wallet_id ON public.ledger_entries(wallet_id);
CREATE INDEX IF NOT EXISTS idx_ledger_transaction_id ON public.ledger_entries(transaction_id);

-- =============================================
-- TABLE: refresh_tokens
-- =============================================
CREATE TABLE IF NOT EXISTS public.refresh_tokens (
    jti uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    device_id character varying(100) NOT NULL,
    device_name character varying(100),
    expires_at timestamp(6) with time zone NOT NULL,
    user_uid uuid NOT NULL,
    CONSTRAINT refresh_tokens_pkey PRIMARY KEY (jti),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_uid) REFERENCES public.users(uid)
    );

CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON public.refresh_tokens(user_uid);
CREATE INDEX IF NOT EXISTS idx_refresh_token_device ON public.refresh_tokens(device_id);

-- =============================================
-- TABLE: device_sessions
-- =============================================
CREATE TABLE IF NOT EXISTS public.device_sessions (
    device_id character varying(255) NOT NULL,
    device_name character varying(255),
    last_login_at timestamp(6) with time zone,
    quick_login_enabled boolean NOT NULL,
    user_uid uuid,
    CONSTRAINT device_sessions_pkey PRIMARY KEY (device_id),
    CONSTRAINT fk_device_session_user FOREIGN KEY (user_uid) REFERENCES public.users(uid)
    );

CREATE INDEX IF NOT EXISTS idx_device_session_user ON public.device_sessions(user_uid);

-- =============================================
-- TABLE: password_reset_tokens
-- =============================================
CREATE TABLE IF NOT EXISTS public.password_reset_tokens (
    id uuid NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    ip_address character varying(255),
    token_hash character varying(255) NOT NULL,
    used boolean NOT NULL,
    used_at timestamp(6) with time zone,
    user_uid uuid NOT NULL,
    CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_uid) REFERENCES public.users(uid)
    );

CREATE INDEX IF NOT EXISTS idx_password_reset_token_hash ON public.password_reset_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_password_reset_user ON public.password_reset_tokens(user_uid);


