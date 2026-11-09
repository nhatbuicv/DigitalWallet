package com.nhatbui.digital_wallet.service.idempotency;

import com.nhatbui.digital_wallet.enums.IdempotencyStatus;
import com.nhatbui.digital_wallet.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyCleanupJob {

    private final IdempotencyRecordRepository repository;

    @Value("${idempotency.in-progress-timeout:5m}")
    private Duration inProgressTimeout;


//    Dọn bản ghi đã có kết quả cuối cùng (COMPLETED/FAILED) và hết TTL. Chạy 1 lần/ngày.
    @Transactional
    @Scheduled(cron = "${idempotency.cleanup-cron:0 0 3 * * *}")
    public void cleanupExpired() {
        int expired = repository.deleteExpired(
                Instant.now(),
                EnumSet.of(IdempotencyStatus.COMPLETED, IdempotencyStatus.FAILED));
        log.info("Idempotency expired cleanup finished: expiredDeleted={}", expired);
    }


    //  Phát hiện bản ghi IN_PROGRESS bị treo và đánh dấu SUSPECTED_TIMEOUT.
//  KHÔNG xóa: giữ lại để chặn request retry thực thi lại (chống trừ tiền 2 lần).
    @Transactional
    @Scheduled(fixedDelayString = "${idempotency.stale-scan-delay:PT1M}")
    public void markStaleInProgress() {
        Instant now = Instant.now();
        int marked = repository.markStaleAsTimeout(now.minus(inProgressTimeout), IdempotencyStatus.IN_PROGRESS, IdempotencyStatus.SUSPECTED_TIMEOUT, now);
        if (marked > 0) {
            log.warn("Marked {} stale IN_PROGRESS idempotency records as SUSPECTED_TIMEOUT (needs reconciliation)", marked);
        }
    }
}
