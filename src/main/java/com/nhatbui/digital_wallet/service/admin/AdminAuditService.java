package com.nhatbui.digital_wallet.service.admin;

import com.nhatbui.digital_wallet.dto.request.admin.AuditLogSearchRequest;
import com.nhatbui.digital_wallet.dto.response.admin.AuditLogResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AdminAuditService {
    PageResponse<AuditLogResponse> getAuditLogs(Pageable pageable, AuditLogSearchRequest searchRequest);
}
