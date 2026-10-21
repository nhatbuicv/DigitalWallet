package com.nhatbui.digital_wallet.service.transaction;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.wallet.DepositRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.TransferRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.WithdrawRequest;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.mapper.TransferMapper;
import com.nhatbui.digital_wallet.repository.LedgerEntryRepository;
import com.nhatbui.digital_wallet.repository.TransactionRepository;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.service.transaction.impl.DepositServiceImpl;
import com.nhatbui.digital_wallet.service.transaction.impl.TransactionFactory;
import com.nhatbui.digital_wallet.service.transaction.impl.TransferServiceImpl;
import com.nhatbui.digital_wallet.service.transaction.impl.WithdrawServiceImpl;
import com.nhatbui.digital_wallet.service.wallet.SystemWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Ví không ACTIVE phải trả đúng mã lý do (khóa/đóng/tạm ngưng) thay vì WALLET_NOT_FOUND,
 * để client phân biệt được 403 (không có quyền dùng ví) với 404 (ví không tồn tại).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WalletStatusErrorCodeTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final long AMOUNT = 25_000L;

    @Mock private TransactionRepository transactionRepository;
    @Mock private TransferMapper transferMapper;
    @Mock private SystemWalletService systemWalletService;
    @Mock private WalletRepository walletRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private LedgerEntryRepository ledgerEntryRepository;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private TransactionFactory transactionFactory;

    private TransferServiceImpl transferService;
    private DepositServiceImpl depositService;
    private WithdrawServiceImpl withdrawService;

    private Wallet fromWallet;
    private Wallet toWallet;
    private Wallet systemWallet;

    @BeforeEach
    void setUp() {
        transferService = new TransferServiceImpl(
                securityUtils,
                walletRepository,
                transactionRepository,
                ledgerEntryRepository,
                transferMapper,
                transactionTemplate,
                transactionFactory);

        depositService = new DepositServiceImpl(
                transactionRepository,
                transferMapper,
                systemWalletService,
                walletRepository,
                securityUtils,
                ledgerEntryRepository,
                transactionTemplate,
                transactionFactory);

        withdrawService = new WithdrawServiceImpl(
                transactionRepository,
                transferMapper,
                systemWalletService,
                walletRepository,
                securityUtils,
                ledgerEntryRepository,
                transactionTemplate,
                transactionFactory);

        fromWallet = wallet("999999999999", "From Wallet", 100_000L);
        toWallet = wallet("888888888888", "To Wallet", 100_000L);
        systemWallet = wallet("555555555555", "System Wallet", 500_000L);

        when(securityUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByUserIdAndIdempotencyKey(eq(USER_ID), anyString()))
                .thenReturn(Optional.empty());
        when(systemWalletService.getSystemWallet()).thenReturn(systemWallet);
        when(walletRepository.findByWalletNumberAndUserUid(anyString(), eq(USER_ID)))
                .thenAnswer(invocation -> Optional.of(fromWallet));
        when(walletRepository.findByWalletNumber(anyString()))
                .thenAnswer(invocation -> Optional.of(toWallet));
        when(walletRepository.findByUidWithLock(any())).thenAnswer(invocation -> {
            UUID uid = invocation.getArgument(0);
            if (uid.equals(fromWallet.getUid())) return Optional.of(fromWallet);
            if (uid.equals(toWallet.getUid())) return Optional.of(toWallet);
            return Optional.empty();
        });
        when(walletRepository.findByWalletNumberWithLock(anyString())).thenAnswer(invocation -> {
            String number = invocation.getArgument(0);
            if (number.equals(systemWallet.getWalletNumber())) return Optional.of(systemWallet);
            if (number.equals(fromWallet.getWalletNumber())) return Optional.of(fromWallet);
            return Optional.of(toWallet);
        });
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });
    }

    @ParameterizedTest
    @EnumSource(value = WalletStatus.class, names = {"INACTIVE", "FROZEN", "CLOSED", "PENDING", "SUSPENDED"})
    void transfer_fromWalletNotActive_returnsStatusSpecificCode(WalletStatus status) {
        fromWallet.setStatus(status);

        assertThatThrownBy(() -> transferService.transfer(transferRequest()))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(expectedCode(status)));
    }

    @ParameterizedTest
    @EnumSource(value = WalletStatus.class, names = {"INACTIVE", "FROZEN", "CLOSED", "PENDING", "SUSPENDED"})
    void transfer_toWalletNotActive_returnsStatusSpecificCode(WalletStatus status) {
        toWallet.setStatus(status);

        assertThatThrownBy(() -> transferService.transfer(transferRequest()))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(expectedCode(status)));
    }

    @ParameterizedTest
    @EnumSource(value = WalletStatus.class, names = {"INACTIVE", "FROZEN", "CLOSED", "PENDING", "SUSPENDED"})
    void deposit_walletNotActive_returnsStatusSpecificCode(WalletStatus status) {
        fromWallet.setStatus(status);

        assertThatThrownBy(() -> depositService.deposit(depositRequest()))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(expectedCode(status)));
    }

    @ParameterizedTest
    @EnumSource(value = WalletStatus.class, names = {"INACTIVE", "FROZEN", "CLOSED", "PENDING", "SUSPENDED"})
    void withdraw_walletNotActive_returnsStatusSpecificCode(WalletStatus status) {
        fromWallet.setStatus(status);

        assertThatThrownBy(() -> withdrawService.withdraw(withdrawRequest()))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(expectedCode(status)));
    }

    @Test
    void transfer_missingWallet_stillReturnsNotFound() {
        when(walletRepository.findByWalletNumberAndUserUid(anyString(), eq(USER_ID)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(transferRequest()))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.WALLET_NOT_FOUND));
    }

    @Test
    void frozenWallet_isForbiddenNotNotFound() {
        assertThat(ErrorCode.WALLET_FROZEN.getStatusCode().value()).isEqualTo(403);
        assertThat(ErrorCode.WALLET_FROZEN.getCode()).isNotEqualTo(ErrorCode.WALLET_NOT_FOUND.getCode());
        assertThat(ErrorCode.WALLET_NOT_FOUND.getStatusCode().value()).isEqualTo(404);
    }

    private ErrorCode expectedCode(WalletStatus status) {
        return switch (status) {
            case FROZEN -> ErrorCode.WALLET_FROZEN;
            case CLOSED -> ErrorCode.WALLET_CLOSED;
            case SUSPENDED -> ErrorCode.WALLET_SUSPENDED;
            default -> ErrorCode.WALLET_NOT_ACTIVE;
        };
    }

    private TransferRequest transferRequest() {
        TransferRequest request = new TransferRequest();
        request.setFromWalletNumber(fromWallet.getWalletNumber());
        request.setToWalletNumber(toWallet.getWalletNumber());
        request.setAmount(AMOUNT);
        request.setDescription("test");
        request.setIdempotencyKey("trf-" + UUID.randomUUID());
        return request;
    }

    private DepositRequest depositRequest() {
        DepositRequest request = new DepositRequest();
        request.setWalletNumber(fromWallet.getWalletNumber());
        request.setAmount(AMOUNT);
        request.setIdempotencyKey("dep-" + UUID.randomUUID());
        return request;
    }

    private WithdrawRequest withdrawRequest() {
        WithdrawRequest request = new WithdrawRequest();
        request.setWalletNumber(fromWallet.getWalletNumber());
        request.setAmount(AMOUNT);
        request.setIdempotencyKey("wit-" + UUID.randomUUID());
        return request;
    }

    private Wallet wallet(String number, String name, long balance) {
        return Wallet.builder()
                .uid(UUID.randomUUID())
                .walletNumber(number)
                .walletName(name)
                .walletType(WalletType.CHECKING)
                .currency(CurrencyCode.VND)
                .balance(balance)
                .availableBalance(balance)
                .blockedBalance(0L)
                .status(WalletStatus.ACTIVE)
                .dailyLimit(50_000_000L)
                .build();
    }
}
