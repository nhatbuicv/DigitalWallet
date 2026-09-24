-- V3__audit_logs.sql
-- Generic admin audit log for important administrative actions

CREATE TABLE IF NOT EXISTS public.audit_logs (
    uid uuid NOT NULL,
    actor_uid uuid NOT NULL,
    actor_name character varying(100),
    action character varying(50) NOT NULL,
    target_type character varying(20) NOT NULL,
    target_uid uuid NOT NULL,
    target_identifier character varying(255),
    detail text,
    created_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT audit_logs_pkey PRIMARY KEY (uid)
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_actor ON public.audit_logs(actor_uid);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON public.audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_target ON public.audit_logs(target_type, target_uid);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON public.audit_logs(created_at);
