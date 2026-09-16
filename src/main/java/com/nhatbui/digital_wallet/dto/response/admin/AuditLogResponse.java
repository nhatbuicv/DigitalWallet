package com.nhatbui.digital_wallet.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhatbui.digital_wallet.enums.AuditAction;
import com.nhatbui.digital_wallet.enums.AuditTargetType;
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
