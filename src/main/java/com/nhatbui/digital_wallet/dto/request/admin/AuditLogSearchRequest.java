package com.nhatbui.digital_wallet.dto.request.admin;

import com.nhatbui.digital_wallet.enums.AuditAction;
import com.nhatbui.digital_wallet.enums.AuditTargetType;
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
