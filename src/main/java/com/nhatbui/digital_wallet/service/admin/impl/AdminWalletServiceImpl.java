package com.nhatbui.digital_wallet.service.admin.impl;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.admin.WalletSearchRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.WalletStatusRequest;
import com.nhatbui.digital_wallet.dto.response.admin.WalletAdminResponse;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.PageResponse;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.entity.WalletStatusHistory;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.mapper.WalletMapper;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.repository.WalletStatusHistoryRepository;
import com.nhatbui.digital_wallet.repository.specification.WalletSpecification;
import com.nhatbui.digital_wallet.service.admin.AdminWalletService;
import com.nhatbui.digital_wallet.service.audit.AuditService;
import com.nhatbui.digital_wallet.enums.AuditAction;
import com.nhatbui.digital_wallet.enums.AuditTargetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminWalletServiceImpl implements AdminWalletService {

    private static final int MAX_OPTIMISTIC_RETRIES = 3;

    private final WalletRepository walletRepository;
    private final WalletStatusHistoryRepository walletStatusHistoryRepository;
    private final WalletMapper walletMapper;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<WalletAdminResponse> getWallets(Pageable pageable, WalletSearchRequest searchRequest) {
        Specification<Wallet> spec = WalletSpecification.buildSearchSpec(searchRequest);
        Page<Wallet> walletPage = walletRepository.findAll(spec, pageable);

        return PageResponse.from(walletPage, walletMapper::mapToWalletAdminResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<WalletAdminResponse> getWalletDetail(UUID walletUid) {
        Wallet wallet = walletRepository.findByUid(walletUid)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        return ApiResponse.<WalletAdminResponse>builder()
                .message("Lấy thông tin ví thành công")
                .success(true)
                .data(walletMapper.mapToWalletAdminResponse(wallet))
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<WalletAdminResponse> freezeWallet(UUID walletUid, WalletStatusRequest statusRequest) {
        validateStatusRequest(statusRequest);
        return changeWalletStatus(walletUid, statusRequest, WalletStatus.FROZEN, "Đóng băng ví thành công");
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<WalletAdminResponse> unfreezeWallet(UUID walletUid, WalletStatusRequest statusRequest) {
        validateStatusRequest(statusRequest);
        return changeWalletStatus(walletUid, statusRequest, WalletStatus.ACTIVE, "Mở khóa ví thành công");
    }

    private void validateStatusRequest(WalletStatusRequest statusRequest) {
        if (statusRequest == null || !StringUtils.hasText(statusRequest.reason())) {
            throw new AppException(ErrorCode.INVALID_REQUEST_BODY);
        }
    }

    private ApiResponse<WalletAdminResponse> changeWalletStatus(
            UUID walletUid,
            WalletStatusRequest statusRequest,
            WalletStatus targetStatus,
            String successMessage) {

        for (int attempt = 0; attempt < MAX_OPTIMISTIC_RETRIES; attempt++) {
            try {
                Wallet wallet = walletRepository.findByUid(walletUid)
                        .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

                validateStateTransition(wallet.getStatus(), targetStatus);

                User admin = securityUtils.getCurrentUser();
                WalletStatus previousStatus = wallet.getStatus();

                wallet.setStatus(targetStatus);
                wallet.setUpdatedBy(admin.getFullName());
                wallet.setUpdatedAt(Instant.now());
                wallet.setClosedReason(statusRequest.reason());
                if (targetStatus == WalletStatus.FROZEN) {
                    wallet.setClosedAt(Instant.now());
                } else if (targetStatus == WalletStatus.ACTIVE) {
                    wallet.setClosedAt(null);
                    wallet.setClosedReason(null);
                }

                Wallet saved = walletRepository.save(wallet);
                recordAudit(saved, previousStatus, targetStatus, statusRequest.reason(), admin.getFullName());

                auditService.log(
                        targetStatus == WalletStatus.FROZEN
                                ? AuditAction.WALLET_FREEZE
                                : AuditAction.WALLET_UNFREEZE,
                        AuditTargetType.WALLET,
                        saved.getUid(),
                        saved.getWalletNumber(),
                        String.format("Thay đổi trạng thái ví từ %s thành %s. Lý do: %s",
                                previousStatus, targetStatus, statusRequest.reason()));

                return ApiResponse.<WalletAdminResponse>builder()
                        .message(successMessage)
                        .success(true)
                        .data(walletMapper.mapToWalletAdminResponse(saved))
                        .build();
            } catch (OptimisticLockingFailureException e) {
                if (attempt == MAX_OPTIMISTIC_RETRIES - 1) {
                    log.warn("Optimistic lock conflict while changing wallet {} status - exhausted retries", walletUid);
                    throw e;
                }
                log.debug("Optimistic lock conflict while changing wallet {} status, retrying ({}/{})",
                        walletUid, attempt + 1, MAX_OPTIMISTIC_RETRIES);
            }
        }
        // Unreachable; defensive fallback
        throw new AppException(ErrorCode.INTERNAL_ERROR);
    }

    private void validateStateTransition(WalletStatus currentStatus, WalletStatus targetStatus) {
        switch (targetStatus) {
            case FROZEN -> {
                if (currentStatus == WalletStatus.FROZEN) {
                    throw new AppException(ErrorCode.INVALID_WALLET_STATUS);
                }
                if (currentStatus != WalletStatus.ACTIVE) {
                    throw new AppException(ErrorCode.WALLET_STATUS_NOT_CHANGEABLE);
                }
            }
            case ACTIVE -> {
                if (currentStatus == WalletStatus.ACTIVE) {
                    throw new AppException(ErrorCode.WALLET_NOT_FROZEN);
                }
                if (currentStatus != WalletStatus.FROZEN) {
                    throw new AppException(ErrorCode.WALLET_STATUS_NOT_CHANGEABLE);
                }
            }
            default -> throw new AppException(ErrorCode.INVALID_ACTION);
        }
    }

    private void recordAudit(Wallet wallet, WalletStatus previousStatus, WalletStatus newStatus,
                             String reason, String changedBy) {
        WalletStatusHistory history = WalletStatusHistory.builder()
                .wallet(wallet)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .changedBy(changedBy)
                .changedAt(Instant.now())
                .build();
        walletStatusHistoryRepository.save(history);
    }
}