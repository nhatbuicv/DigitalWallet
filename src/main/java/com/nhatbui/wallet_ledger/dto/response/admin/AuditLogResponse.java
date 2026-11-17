package com.nhatbui.wallet_ledger.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhatbui.wallet_ledger.enums.AuditAction;
import com.nhatbui.wallet_ledger.enums.AuditTargetType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AuditLogResponse {
    private UUID uid;
    private UUID actorUid;
    private String actorName;
    private AuditAction action;
    private AuditTargetType targetType;
    private UUID targetUid;
    private String targetIdentifier;
    private String detail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Instant createdAt;
}
