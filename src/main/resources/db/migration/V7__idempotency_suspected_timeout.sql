-- V7__idempotency_suspected_timeout.sql
-- Thêm trạng thái SUSPECTED_TIMEOUT (17 ký tự) -> mở rộng cột status.
-- Bản ghi IN_PROGRESS quá hạn sẽ được đánh dấu SUSPECTED_TIMEOUT thay vì bị xóa cứng.

ALTER TABLE public.idempotency_records
    ALTER COLUMN status TYPE varchar(32);
