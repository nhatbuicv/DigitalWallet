package com.nhatbui.wallet_ledger.dto.request.admin;

import com.nhatbui.wallet_ledger.enums.AuditAction;
import com.nhatbui.wallet_ledger.enums.AuditTargetType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSearchRequest {
    private UUID actorUid;
    private AuditAction action;
    private AuditTargetType targetType;
    private UUID targetUid;
    private Instant fromDate;
    private Instant toDate;
}
