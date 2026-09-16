package com.nhatbui.digital_wallet.service.audit;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.entity.AuditLog;
import com.nhatbui.digital_wallet.enums.AuditAction;
import com.nhatbui.digital_wallet.enums.AuditTargetType;
import com.nhatbui.digital_wallet.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action,
                    AuditTargetType targetType,
                    UUID targetUid,
                    String targetIdentifier,
                    String detail) {
        try {
            UUID actorUid;
            String actorName;
            try {
                actorUid = securityUtils.getCurrentUserId();
                actorName = securityUtils.getCurrentUser().getFullName();
            } catch (Exception e) {
                actorUid = null;
                actorName = "SYSTEM";
            }

            AuditLog logEntry = AuditLog.builder()
                    .actorUid(actorUid)
                    .actorName(actorName)
                    .action(action)
                    .targetType(targetType)
                    .targetUid(targetUid)
                    .targetIdentifier(targetIdentifier)
                    .detail(detail)
                    .createdAt(Instant.now())
                    .build();
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to write audit log for action {} on {} {}", action, targetType, targetUid, e);
        }
    }
}
