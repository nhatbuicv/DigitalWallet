package com.nhatbui.wallet_ledger.service.admin;

import com.nhatbui.wallet_ledger.dto.request.admin.AuditLogSearchRequest;
import com.nhatbui.wallet_ledger.dto.response.admin.AuditLogResponse;
import com.nhatbui.wallet_ledger.dto.response.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AdminAuditService {
    PageResponse<AuditLogResponse> getAuditLogs(Pageable pageable, AuditLogSearchRequest searchRequest);
}
