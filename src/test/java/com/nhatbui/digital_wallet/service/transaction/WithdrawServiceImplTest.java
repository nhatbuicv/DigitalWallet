package com.nhatbui.digital_wallet.service.transaction;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.wallet.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.entity.LedgerEntry;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import com.nhatbui.digital_wallet.enums.EntryType;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.generator.ReferenceCodeGenerator;
import com.nhatbui.digital_wallet.mapper.TransferMapper;
import com.nhatbui.digital_wallet.repository.LedgerEntryRepository;
import com.nhatbui.digital_wallet.repository.TransactionRepository;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.service.idempotency.ClaimResult;
import com.nhatbui.digital_wallet.service.idempotency.ClaimResult.ClaimStatus;
import com.nhatbui.digital_wallet.service.idempotency.IdempotencyService;
import com.nhatbui.digital_wallet.service.idempotency.IdempotentExecutor;
import com.nhatbui.digital_wallet.service.idempotency.RequestHasher;
import com.nhatbui.digital_wallet.service.transaction.impl.TransactionFactory;
import com.nhatbui.digital_wallet.service.transaction.impl.WithdrawServiceImpl;
import com.nhatbui.digital_wallet.service.wallet.SystemWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression cho bug "lệch availableBalance khi withdraw": code cũ ghi
 * {@code setAvailableBalance} nhầm sang ví user và không bao giờ cập nhật
 * availableBalance của ví system, khiến balance/availableBalance hai ví lệch nhau.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WithdrawServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final long USER_BALANCE = 100_000L;
    private static final long SYSTEM_BALANCE = 500_000L;
    private static final long AMOUNT = 25_000L;

    @Mock private TransactionRepository transactionRepository;
    @Mock private TransferMapper transferMapper;
    @Mock private SystemWalletService systemWalletService;
    @Mock private WalletRepository walletRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private LedgerEntryRepository ledgerEntryRepository;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private IdempotencyService idempotencyService;

    private final RequestHasher requestHasher = new RequestHasher();

    private WithdrawServiceImpl withdrawService;

    private Wallet userWallet;
    private Wallet systemWallet;

    @BeforeEach
    void setUp() {
        IdempotentExecutor idempotentExecutor = new IdempotentExecutor(idempotencyService, transactionTemplate);

        withdrawService = new WithdrawServiceImpl(
                transactionRepository,
                transferMapper,
                systemWalletService,
                walletRepository,
                securityUtils,
                ledgerEntryRepository,
                new TransactionFactory(new ReferenceCodeGenerator()),
                idempotentExecutor,
                requestHasher);

        userWallet = wallet("999999999999", "User Wallet", USER_BALANCE);
        systemWallet = wallet("555555555555", "System Wallet", SYSTEM_BALANCE);

        when(securityUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(idempotencyService.claim(eq(USER_ID), any(), anyString(), anyString()))
                .thenAnswer(inv -> new ClaimResult(UUID.randomUUID(), ClaimStatus.OWNED, null, null));
        when(systemWalletService.getSystemWallet()).thenReturn(systemWallet);
        when(walletRepository.findByWalletNumberAndUserUid(anyString(), eq(USER_ID)))
                .thenReturn(Optional.of(userWallet));
        when(walletRepository.findByWalletNumberWithLock(anyString()))
                .thenAnswer(invocation -> {
                    String number = invocation.getArgument(0);
                    return Optional.of(number.equals(systemWallet.getWalletNumber()) ? systemWallet : userWallet);
                });
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferMapper.mapToTransferResponse(any())).thenReturn(TransferResponse.builder().build());

        // TransactionTemplate.execute phải thực thi callback để chạy doWithdraw.
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });
    }

    @Test
    void withdraw_keepsBalanceAndAvailableBalanceInSyncForBothWallets() {
        withdrawService.withdraw(request(AMOUNT)).body();

        // Ví user: trừ cả balance và availableBalance.
        assertThat(userWallet.getBalance()).isEqualTo(USER_BALANCE - AMOUNT);
        assertThat(userWallet.getAvailableBalance()).isEqualTo(USER_BALANCE - AMOUNT);

        // Ví system: cộng cả balance và availableBalance (bug cũ bỏ sót availableBalance).
        assertThat(systemWallet.getBalance()).isEqualTo(SYSTEM_BALANCE + AMOUNT);
        assertThat(systemWallet.getAvailableBalance()).isEqualTo(SYSTEM_BALANCE + AMOUNT);
    }

    @Test
    void withdraw_conservesTotalMoneyAcrossBothWallets() {
        long totalBefore = userWallet.getBalance() + systemWallet.getBalance();

        withdrawService.withdraw(request(AMOUNT)).body();

        long totalAfter = userWallet.getBalance() + systemWallet.getBalance();
        assertThat(totalAfter).isEqualTo(totalBefore);
    }

    @Test
    @SuppressWarnings("unchecked")
    void withdraw_recordsBalancedDoubleEntryWithBalanceBefore() {
        withdrawService.withdraw(request(AMOUNT)).body();

        ArgumentCaptor<List<LedgerEntry>> captor = ArgumentCaptor.forClass(List.class);
        verify(ledgerEntryRepository).saveAll(captor.capture());

        List<LedgerEntry> entries = captor.getValue();
        assertThat(entries).hasSize(2);

        LedgerEntry debitUser = entries.stream()
                .filter(e -> e.getEntryType() == EntryType.DEBIT)
                .findFirst().orElseThrow();
        LedgerEntry creditSystem = entries.stream()
                .filter(e -> e.getEntryType() == EntryType.CREDIT)
                .findFirst().orElseThrow();

        assertThat(debitUser.getWallet().getWalletNumber()).isEqualTo(userWallet.getWalletNumber());
        assertThat(debitUser.getBalanceBefore()).isEqualTo(USER_BALANCE);
        assertThat(debitUser.getBalanceAfter()).isEqualTo(USER_BALANCE - AMOUNT);

        assertThat(creditSystem.getWallet().getWalletNumber()).isEqualTo(systemWallet.getWalletNumber());
        assertThat(creditSystem.getBalanceBefore()).isEqualTo(SYSTEM_BALANCE);
        assertThat(creditSystem.getBalanceAfter()).isEqualTo(SYSTEM_BALANCE + AMOUNT);

        // Double-entry: tổng debit = tổng credit.
        assertThat(debitUser.getAmount()).isEqualTo(creditSystem.getAmount());
    }

    @Test
    void withdraw_businessError_callsIdempotencyFail() {
        UUID claimId = UUID.randomUUID();
        when(idempotencyService.claim(eq(USER_ID), any(), anyString(), anyString()))
                .thenReturn(new ClaimResult(claimId, ClaimStatus.OWNED, null, null));

        // Withdraw amount < 1000 throws INVALID_WITHDRAW_AMOUNT (400 Bad Request)
        assertThatThrownBy(() -> withdrawService.withdraw(request(500L)))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_WITHDRAW_AMOUNT));

        verify(idempotencyService).fail(claimId, ErrorCode.INVALID_WITHDRAW_AMOUNT);
    }

    @Test
    void withdraw_internalServerError_callsIdempotencyRelease() {
        UUID claimId = UUID.randomUUID();
        when(idempotencyService.claim(eq(USER_ID), any(), anyString(), anyString()))
                .thenReturn(new ClaimResult(claimId, ClaimStatus.OWNED, null, null));
        doThrow(new AppException(ErrorCode.INTERNAL_ERROR))
                .when(transactionTemplate).execute(any());

        assertThatThrownBy(() -> withdrawService.withdraw(request(AMOUNT)))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR));

        verify(idempotencyService).release(claimId);
    }

    @Test
    void withdraw_unexpectedRuntimeException_callsIdempotencyRelease() {
        UUID claimId = UUID.randomUUID();
        when(idempotencyService.claim(eq(USER_ID), any(), anyString(), anyString()))
                .thenReturn(new ClaimResult(claimId, ClaimStatus.OWNED, null, null));
        doThrow(new IllegalStateException("DB connection pool exhausted"))
                .when(transactionTemplate).execute(any());

        assertThatThrownBy(() -> withdrawService.withdraw(request(AMOUNT)))
                .isInstanceOf(IllegalStateException.class);

        verify(idempotencyService).release(claimId);
    }

    private WithdrawRequest request(long amount) {
        WithdrawRequest request = new WithdrawRequest();
        request.setWalletNumber(userWallet.getWalletNumber());
        request.setAmount(amount);
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
