package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.admin.AuditLogSearchRequest;
import com.nhatbui.digital_wallet.dto.response.admin.AuditLogAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.enums.AuditAction;
import com.nhatbui.digital_wallet.enums.AuditTargetType;
import com.nhatbui.digital_wallet.service.admin.AdminAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogAdminResponse>>> getAuditLogs(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) UUID actorUid,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) AuditTargetType targetType,
            @RequestParam(required = false) UUID targetUid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate) {

        AuditLogSearchRequest searchRequest = AuditLogSearchRequest.builder()
                .actorUid(actorUid)
                .action(action)
                .targetType(targetType)
                .targetUid(targetUid)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        PageResponse<AuditLogAdminResponse> data = adminAuditService.getAuditLogs(pageable, searchRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<PageResponse<AuditLogAdminResponse>>builder()
                        .message("Lấy nhật ký kiểm toán thành công")
                        .success(true)
                        .data(data)
                        .build());
    }
}
