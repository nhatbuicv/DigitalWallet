package com.nhatbui.digital_wallet.service.admin.impl;

import com.nhatbui.digital_wallet.dto.request.admin.AuditLogSearchRequest;
import com.nhatbui.digital_wallet.dto.response.admin.AuditLogResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.entity.AuditLog;
import com.nhatbui.digital_wallet.repository.AuditLogRepository;
import com.nhatbui.digital_wallet.repository.specification.AuditLogSpecification;
import com.nhatbui.digital_wallet.service.admin.AdminAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AdminAuditServiceImpl implements AdminAuditService {

    private final AuditLogRepository auditLogRepository;

    private static final Function<AuditLog, AuditLogResponse> TO_RESPONSE = log -> AuditLogResponse.builder()
            .uid(log.getUid())
            .actorUid(log.getActorUid())
            .actorName(log.getActorName())
            .action(log.getAction())
            .targetType(log.getTargetType())
            .targetUid(log.getTargetUid())
            .targetIdentifier(log.getTargetIdentifier())
            .detail(log.getDetail())
            .createdAt(log.getCreatedAt())
            .build();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<AuditLogResponse> getAuditLogs(Pageable pageable, AuditLogSearchRequest searchRequest) {
        Specification<AuditLog> spec = AuditLogSpecification.buildSearchSpec(searchRequest);
        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);
        return PageResponse.from(page, TO_RESPONSE);
    }
}
