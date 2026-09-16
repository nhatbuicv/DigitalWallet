package com.nhatbui.digital_wallet.service.admin;

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
import com.nhatbui.digital_wallet.service.admin.impl.AdminWalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminWalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletStatusHistoryRepository walletStatusHistoryRepository;

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private com.nhatbui.digital_wallet.service.audit.AuditService auditService;

    private AdminWalletServiceImpl adminWalletService;

    private static final UUID WALLET_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID ADMIN_UID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

    private Wallet activeWallet;
    private Wallet frozenWallet;
    private User adminUser;
    private WalletAdminResponse response;

    @BeforeEach
    void setUp() {
        adminWalletService = new AdminWalletServiceImpl(
                walletRepository,
                walletStatusHistoryRepository,
                walletMapper,
                securityUtils,
                auditService
        );

        adminUser = User.builder()
                .uid(ADMIN_UID)
                .fullName("Admin User")
                .email("admin@example.com")
                .phone("0912345678")
                .build();

        activeWallet = Wallet.builder()
                .uid(WALLET_UID)
                .walletNumber("123456789012")
                .walletName("Test Wallet")
                .status(WalletStatus.ACTIVE)
                .version(1)
                .build();

        frozenWallet = Wallet.builder()
                .uid(WALLET_UID)
                .walletNumber("123456789012")
                .walletName("Test Wallet")
                .status(WalletStatus.FROZEN)
                .version(1)
                .build();

        response = WalletAdminResponse.builder()
                .uid(WALLET_UID.toString())
                .walletNumber("123456789012")
                .walletName("Test Wallet")
                .status("ACTIVE")
                .build();
    }

    // ========== getWallets ==========

    @Test
    void getWallets_withFilters_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Wallet> walletPage = new PageImpl<>(List.of(activeWallet), pageable, 1);

        when(walletRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(walletPage);
        when(walletMapper.mapToWalletAdminResponse(activeWallet)).thenReturn(response);

        WalletSearchRequest searchRequest = WalletSearchRequest.builder()
                .walletNumber("123456789012")
                .status(WalletStatus.ACTIVE)
                .build();

        PageResponse<WalletAdminResponse> result = adminWalletService.getWallets(pageable, searchRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getWalletNumber()).isEqualTo("123456789012");
    }

    @Test
    void getWallets_withNoFilters_shouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Wallet> walletPage = new PageImpl<>(List.of(activeWallet, frozenWallet), pageable, 2);

        when(walletRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(walletPage);
        when(walletMapper.mapToWalletAdminResponse(activeWallet)).thenReturn(response);

        WalletSearchRequest searchRequest = WalletSearchRequest.builder().build();
        PageResponse<WalletAdminResponse> result = adminWalletService.getWallets(pageable, searchRequest);

        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(walletRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ========== getWalletDetail ==========

    @Test
    void getWalletDetail_withExistingUid_shouldReturnWallet() {
        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.of(activeWallet));
        when(walletMapper.mapToWalletAdminResponse(activeWallet)).thenReturn(response);

        ApiResponse<WalletAdminResponse> result = adminWalletService.getWalletDetail(WALLET_UID);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getWalletNumber()).isEqualTo("123456789012");
    }

    @Test
    void getWalletDetail_withNonExistingUid_shouldThrowException() {
        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminWalletService.getWalletDetail(WALLET_UID))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WALLET_NOT_FOUND);
    }

    // ========== freezeWallet ==========

    @Test
    void freezeWallet_withActiveWallet_shouldFreezeAndRecordAudit() {
        WalletStatusRequest request = new WalletStatusRequest("Fraudulent activity detected");
        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.of(activeWallet));
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletMapper.mapToWalletAdminResponse(any(Wallet.class))).thenReturn(response);

        ApiResponse<WalletAdminResponse> result = adminWalletService.freezeWallet(WALLET_UID, request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Đóng băng ví thành công");
        assertThat(activeWallet.getStatus()).isEqualTo(WalletStatus.FROZEN);
        assertThat(activeWallet.getClosedReason()).isEqualTo("Fraudulent activity detected");
        assertThat(activeWallet.getUpdatedBy()).isEqualTo("Admin User");
        assertThat(activeWallet.getClosedAt()).isNotNull();

        verify(walletRepository).save(activeWallet);
        verify(walletStatusHistoryRepository).save(any());
    }

    @Test
    void freezeWallet_withAlreadyFrozenWallet_shouldThrowException() {
        WalletStatusRequest request = new WalletStatusRequest("Fraudulent activity");
        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.of(frozenWallet));

        assertThatThrownBy(() -> adminWalletService.freezeWallet(WALLET_UID, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_WALLET_STATUS);
        verify(walletRepository, never()).save(any());
        verify(walletStatusHistoryRepository, never()).save(any());
    }

    @Test
    void freezeWallet_withClosedWallet_shouldThrowException() {
        Wallet wallet = Wallet.builder()
                .uid(WALLET_UID)
                .walletNumber("123456789012")
                .walletName("Test Wallet")
                .status(WalletStatus.CLOSED)
                .build();
        WalletStatusRequest request = new WalletStatusRequest("Fraudulent activity");

        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> adminWalletService.freezeWallet(WALLET_UID, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WALLET_STATUS_NOT_CHANGEABLE);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void freezeWallet_withNonExistingUid_shouldThrowException() {
        WalletStatusRequest request = new WalletStatusRequest("Fraudulent activity");
        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminWalletService.freezeWallet(WALLET_UID, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WALLET_NOT_FOUND);
    }

    @Test
    void freezeWallet_withNullRequest_shouldThrowInvalidRequest() {
        assertThatThrownBy(() -> adminWalletService.freezeWallet(WALLET_UID, null))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST_BODY);
    }

    @Test
    void freezeWallet_withEmptyReason_shouldThrowInvalidRequest() {
        WalletStatusRequest request = new WalletStatusRequest("");
        assertThatThrownBy(() -> adminWalletService.freezeWallet(WALLET_UID, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST_BODY);
    }

    // ========== unfreezeWallet ==========

    @Test
    void unfreezeWallet_withFrozenWallet_shouldUnfreezeAndRecordAudit() {
        WalletStatusRequest request = new WalletStatusRequest("Issue resolved");
        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.of(frozenWallet));
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletMapper.mapToWalletAdminResponse(any(Wallet.class))).thenReturn(response);

        ApiResponse<WalletAdminResponse> result = adminWalletService.unfreezeWallet(WALLET_UID, request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Mở khóa ví thành công");
        assertThat(frozenWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        assertThat(frozenWallet.getUpdatedBy()).isEqualTo("Admin User");

        verify(walletRepository).save(frozenWallet);
        verify(walletStatusHistoryRepository).save(any());
    }

    @Test
    void unfreezeWallet_withActiveWallet_shouldThrowException() {
        WalletStatusRequest request = new WalletStatusRequest("Issue resolved");
        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.of(activeWallet));

        assertThatThrownBy(() -> adminWalletService.unfreezeWallet(WALLET_UID, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WALLET_NOT_FROZEN);
        verify(walletRepository, never()).save(any());
        verify(walletStatusHistoryRepository, never()).save(any());
    }

    @Test
    void unfreezeWallet_withNonFrozenWallet_shouldThrowException() {
        Wallet suspendedWallet = Wallet.builder()
                .uid(WALLET_UID)
                .walletNumber("123456789012")
                .walletName("Test Wallet")
                .status(WalletStatus.SUSPENDED)
                .build();
        WalletStatusRequest request = new WalletStatusRequest("Issue resolved");

        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.of(suspendedWallet));

        assertThatThrownBy(() -> adminWalletService.unfreezeWallet(WALLET_UID, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WALLET_STATUS_NOT_CHANGEABLE);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void unfreezeWallet_withNonExistingUid_shouldThrowException() {
        WalletStatusRequest request = new WalletStatusRequest("Issue resolved");
        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminWalletService.unfreezeWallet(WALLET_UID, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WALLET_NOT_FOUND);
    }

    // ========== Optimistic Locking ==========

    @Test
    void freezeWallet_withOptimisticLockConflict_shouldRetryAndSucceed() {
        WalletStatusRequest request = new WalletStatusRequest("Fraudulent activity detected");
        when(walletRepository.findByUid(WALLET_UID))
                .thenAnswer(invocation -> Optional.of(activeWalletCopy()));
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new OptimisticLockingFailureException("Version conflict"))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(walletMapper.mapToWalletAdminResponse(any(Wallet.class))).thenReturn(response);

        ApiResponse<WalletAdminResponse> result = adminWalletService.freezeWallet(WALLET_UID, request);

        assertThat(result.isSuccess()).isTrue();
        verify(walletRepository, times(2)).findByUid(WALLET_UID);
        verify(walletRepository, times(2)).save(any(Wallet.class));
    }

    @Test
    void freezeWallet_withOptimisticLockConflictExhaustedRetries_shouldThrow() {
        WalletStatusRequest request = new WalletStatusRequest("Fraudulent activity detected");
        when(walletRepository.findByUid(WALLET_UID))
                .thenAnswer(invocation -> Optional.of(activeWalletCopy()));
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new OptimisticLockingFailureException("Version conflict"));

        assertThatThrownBy(() -> adminWalletService.freezeWallet(WALLET_UID, request))
                .isInstanceOf(OptimisticLockingFailureException.class);
        verify(walletRepository, times(3)).findByUid(WALLET_UID);
        verify(walletRepository, times(3)).save(any(Wallet.class));
        verifyNoInteractions(auditService);
    }

    private Wallet activeWalletCopy() {
        return Wallet.builder()
                .uid(WALLET_UID)
                .walletNumber("123456789012")
                .walletName("Test Wallet")
                .status(WalletStatus.ACTIVE)
                .version(1)
                .build();
    }

    // ========== Audit Recording ==========

    @Test
    void freezeWallet_shouldRecordAuditWithCorrectFields() {
        WalletStatusRequest request = new WalletStatusRequest("Fraudulent activity detected");
        when(walletRepository.findByUid(WALLET_UID)).thenReturn(Optional.of(activeWallet));
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletMapper.mapToWalletAdminResponse(any(Wallet.class))).thenReturn(response);

        adminWalletService.freezeWallet(WALLET_UID, request);

        ArgumentCaptor<WalletStatusHistory> captor = ArgumentCaptor.forClass(WalletStatusHistory.class);
        verify(walletStatusHistoryRepository).save(captor.capture());
        WalletStatusHistory history = captor.getValue();
        assertThat(history.getPreviousStatus()).isEqualTo(WalletStatus.ACTIVE);
        assertThat(history.getNewStatus()).isEqualTo(WalletStatus.FROZEN);
        assertThat(history.getReason()).isEqualTo("Fraudulent activity detected");
        assertThat(history.getChangedBy()).isEqualTo("Admin User");
        assertThat(history.getChangedAt()).isNotNull();
    }
}