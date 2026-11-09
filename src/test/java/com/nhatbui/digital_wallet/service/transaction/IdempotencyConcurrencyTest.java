package com.nhatbui.digital_wallet.service.transaction;

import com.nhatbui.digital_wallet.AbstractIntegrationTest;
import com.nhatbui.digital_wallet.dto.request.wallet.DepositRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.TransferRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.IdempotentResult;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.entity.IdempotencyRecord;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import com.nhatbui.digital_wallet.enums.IdempotencyOperation;
import com.nhatbui.digital_wallet.enums.IdempotencyStatus;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.repository.IdempotencyRecordRepository;
import com.nhatbui.digital_wallet.repository.LedgerEntryRepository;
import com.nhatbui.digital_wallet.repository.TransactionRepository;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.service.idempotency.ClaimResult;
import com.nhatbui.digital_wallet.service.idempotency.IdempotencyCleanupJob;
import com.nhatbui.digital_wallet.service.idempotency.IdempotencyService;
import com.nhatbui.digital_wallet.service.idempotency.RequestHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Kiểm thử Claim-first Idempotency cho Transfer, Deposit, Withdraw.
 */
class IdempotencyConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private DepositService depositService;

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private IdempotencyRecordRepository idempotencyRecordRepository;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private RequestHasher requestHasher;

    @Autowired
    private IdempotencyCleanupJob cleanupJob;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID userId;
    private Wallet wallet;
    private Wallet secondWallet;

    @BeforeEach
    void setUp() {
        User user = createUser("idem");
        userId = user.getUid();
        wallet = createWallet(user, 100_000L);

        User receiver = createUser("recv");
        secondWallet = createWallet(receiver, 50_000L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void transfer_firstAndReplay_createsSingleTransactionAndLedgerEntries() {
        authenticate(userId);
        String key = "trf-" + UUID.randomUUID();
        TransferRequest request = new TransferRequest();
        request.setFromWalletNumber(wallet.getWalletNumber());
        request.setToWalletNumber(secondWallet.getWalletNumber());
        request.setAmount(10_000L);
        request.setDescription("test transfer");
        request.setIdempotencyKey(key);

        IdempotentResult<ApiResponse<TransferResponse>> first = transferService.transfer(request);
        assertThat(first.replayed()).isFalse();
        assertThat(first.body().isSuccess()).isTrue();

        IdempotentResult<ApiResponse<TransferResponse>> second = transferService.transfer(request);
        assertThat(second.replayed()).isTrue();
        assertThat(second.body().getData().getReferenceCode())
                .isEqualTo(first.body().getData().getReferenceCode());

        assertThat(transactionRepository.countByUserIdAndIdempotencyKey(userId, key)).isEqualTo(1);
        IdempotencyRecord record = idempotencyRecordRepository
                .findByUserIdAndOperationAndIdemKey(userId, IdempotencyOperation.TRANSFER, key)
                .orElseThrow();
        assertThat(record.getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(walletRepository.findByUid(wallet.getUid()).orElseThrow().getBalance()).isEqualTo(90_000L);
        assertThat(walletRepository.findByUid(secondWallet.getUid()).orElseThrow().getBalance()).isEqualTo(60_000L);
    }

    @Test
    void sameKey_differentPayload_throwsKeyReused409() {
        authenticate(userId);
        String key = "conflict-" + UUID.randomUUID();

        DepositRequest first = depositRequest(wallet.getWalletNumber(), key);
        first.setAmount(1_000L);
        depositService.deposit(first);

        DepositRequest different = depositRequest(wallet.getWalletNumber(), key);
        different.setAmount(2_000L);

        assertThatThrownBy(() -> depositService.deposit(different))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.IDEMPOTENCY_KEY_REUSED));
    }

    @Test
    void sameKey_differentOperation_areIndependent() {
        authenticate(userId);
        String key = "multi-op-" + UUID.randomUUID();

        DepositRequest dep = depositRequest(wallet.getWalletNumber(), key);
        IdempotentResult<ApiResponse<TransferResponse>> depResult = depositService.deposit(dep);

        WithdrawRequest wit = new WithdrawRequest();
        wit.setWalletNumber(wallet.getWalletNumber());
        wit.setAmount(1_000L);
        wit.setIdempotencyKey(key);
        IdempotentResult<ApiResponse<TransferResponse>> witResult = withdrawService.withdraw(wit);

        assertThat(depResult.replayed()).isFalse();
        assertThat(witResult.replayed()).isFalse();
        assertThat(depResult.body().getData().getReferenceCode())
                .isNotEqualTo(witResult.body().getData().getReferenceCode());
    }

    @Test
    void businessFailure_marksFailedAndReplaysSameError() {
        authenticate(userId);
        String key = "fail-" + UUID.randomUUID();

        WithdrawRequest request = new WithdrawRequest();
        request.setWalletNumber(wallet.getWalletNumber());
        request.setAmount(999_999L);
        request.setIdempotencyKey(key);

        assertThatThrownBy(() -> withdrawService.withdraw(request))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE));

        IdempotencyRecord record = idempotencyRecordRepository
                .findByUserIdAndOperationAndIdemKey(userId, IdempotencyOperation.WITHDRAW, key)
                .orElseThrow();
        assertThat(record.getStatus()).isEqualTo(IdempotencyStatus.FAILED);

        // Ngay cả khi ví được nạp thêm tiền, retry cùng key vẫn replay lỗi cũ
        wallet.setBalance(2_000_000L);
        wallet.setAvailableBalance(2_000_000L);
        walletRepository.save(wallet);

        assertThatThrownBy(() -> withdrawService.withdraw(request))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE));
    }

    @Test
    void staleInProgress_markedSuspectedTimeout_retryBlocked_noDoubleDeduct() {
        authenticate(userId);
        String key = "stale-" + UUID.randomUUID();
        WithdrawRequest request = new WithdrawRequest();
        request.setWalletNumber(wallet.getWalletNumber());
        request.setAmount(10_000L);
        request.setIdempotencyKey(key);
        String hash = requestHasher.hash(
                IdempotencyOperation.WITHDRAW,
                request.getWalletNumber(),
                request.getAmount(),
                request.getDescription());

        // Request 1 giành claim nhưng xử lý rất chậm (giả lập treo)
        ClaimResult claim = idempotencyService.claim(userId, IdempotencyOperation.WITHDRAW, key, hash);
        assertThat(claim.status()).isEqualTo(ClaimResult.ClaimStatus.OWNED);

        // Khi đang IN_PROGRESS -> request tiếp theo bị 409 IDEMPOTENCY_IN_PROGRESS
        assertThatThrownBy(() -> withdrawService.withdraw(request))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.IDEMPOTENCY_IN_PROGRESS));

        // Giả lập claim bị treo quá in-progress-timeout -> job quét stale
        backdateUpdatedAt(claim.recordId(), Duration.ofMinutes(15));
        cleanupJob.markStaleInProgress();

        // Bản ghi KHÔNG bị xóa, chỉ chuyển sang SUSPECTED_TIMEOUT
        IdempotencyRecord record = idempotencyRecordRepository.findById(claim.recordId()).orElseThrow();
        assertThat(record.getStatus()).isEqualTo(IdempotencyStatus.SUSPECTED_TIMEOUT);

        // Retry cùng key bị chặn -> không thực thi lần 2
        assertThatThrownBy(() -> withdrawService.withdraw(request))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.TRANSACTION_STATUS_UNCERTAIN));

        // Job dọn expired hằng ngày cũng không được xóa bản ghi nghi ngờ, kể cả khi hết TTL
        transactionTemplate.executeWithoutResult(status -> {
            IdempotencyRecord rec = idempotencyRecordRepository.findById(claim.recordId()).orElseThrow();
            rec.setExpiresAt(Instant.now().minus(Duration.ofHours(1)));
            idempotencyRecordRepository.save(rec);
        });
        cleanupJob.cleanupExpired();
        assertThat(idempotencyRecordRepository.findById(claim.recordId())).isPresent();
        assertThatThrownBy(() -> withdrawService.withdraw(request))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.TRANSACTION_STATUS_UNCERTAIN));

        // Không có giao dịch trừ tiền nào được tạo bởi các lần retry
        assertThat(transactionRepository.countByUserIdAndIdempotencyKey(userId, key)).isZero();
        assertThat(walletRepository.findByUid(wallet.getUid()).orElseThrow().getBalance()).isEqualTo(100_000L);
    }

    @Test
    void slowOriginalRequest_completesAfterMarkedSuspected_retryReplaysResult() {
        authenticate(userId);
        String key = "late-" + UUID.randomUUID();
        DepositRequest request = depositRequest(wallet.getWalletNumber(), key);
        String hash = requestHasher.hash(
                IdempotencyOperation.DEPOSIT,
                request.getWalletNumber(),
                request.getAmount(),
                request.getDescription());

        ClaimResult claim = idempotencyService.claim(userId, IdempotencyOperation.DEPOSIT, key, hash);
        backdateUpdatedAt(claim.recordId(), Duration.ofMinutes(15));
        cleanupJob.markStaleInProgress();
        assertThat(idempotencyRecordRepository.findById(claim.recordId()).orElseThrow().getStatus())
                .isEqualTo(IdempotencyStatus.SUSPECTED_TIMEOUT);

        // Request 1 (chậm) cuối cùng cũng hoàn tất -> ghi kết quả lên chính bản ghi đó
        ApiResponse<TransferResponse> lateResult = ApiResponse.<TransferResponse>builder()
                .success(true)
                .code(1000)
                .message("ok")
                .data(TransferResponse.builder().referenceCode("LATE-REF").build())
                .build();
        transactionTemplate.executeWithoutResult(status ->
                idempotencyService.complete(claim.recordId(), 200, lateResult));

        // Retry sau đó được replay kết quả cũ, không tạo giao dịch mới
        IdempotentResult<ApiResponse<TransferResponse>> retried = depositService.deposit(request);
        assertThat(retried.replayed()).isTrue();
        assertThat(retried.body().getData().getReferenceCode()).isEqualTo("LATE-REF");
        assertThat(transactionRepository.countByUserIdAndIdempotencyKey(userId, key)).isZero();
    }

    @Test
    void cleanupExpired_deletesOnlyFinalRecords() {
        authenticate(userId);
        String key = "expired-" + UUID.randomUUID();
        depositService.deposit(depositRequest(wallet.getWalletNumber(), key));
        IdempotencyRecord completed = idempotencyRecordRepository
                .findByUserIdAndOperationAndIdemKey(userId, IdempotencyOperation.DEPOSIT, key)
                .orElseThrow();

        String inProgressKey = "expired-ip-" + UUID.randomUUID();
        ClaimResult inProgress = idempotencyService.claim(userId, IdempotencyOperation.DEPOSIT, inProgressKey, "x".repeat(64));

        transactionTemplate.executeWithoutResult(status -> {
            for (UUID id : List.of(completed.getId(), inProgress.recordId())) {
                IdempotencyRecord rec = idempotencyRecordRepository.findById(id).orElseThrow();
                rec.setExpiresAt(Instant.now().minus(Duration.ofHours(1)));
                idempotencyRecordRepository.save(rec);
            }
        });

        cleanupJob.cleanupExpired();

        assertThat(idempotencyRecordRepository.findById(completed.getId())).isEmpty();
        assertThat(idempotencyRecordRepository.findById(inProgress.recordId())).isPresent();
    }

    private void backdateUpdatedAt(UUID recordId, Duration age) {
        transactionTemplate.executeWithoutResult(status -> {
            IdempotencyRecord rec = idempotencyRecordRepository.findById(recordId).orElseThrow();
            rec.setUpdatedAt(Instant.now().minus(age));
            idempotencyRecordRepository.save(rec);
        });
    }

    @Test
    void deposit_concurrent_sameKey_createsOneTransaction() throws Exception {
        String key = "dep-" + UUID.randomUUID();
        DepositRequest request = new DepositRequest();
        request.setWalletNumber(wallet.getWalletNumber());
        request.setAmount(1000L);
        request.setIdempotencyKey(key);

        List<String> referenceCodes = runConcurrently(10, () -> {
            authenticate(userId);
            try {
                TransferResponse response = depositService.deposit(request).body().getData();
                return response.getReferenceCode();
            } catch (AppException e) {
                if (e.getErrorCode() == ErrorCode.IDEMPOTENCY_IN_PROGRESS) {
                    return "IN_PROGRESS";
                }
                throw e;
            }
        });

        Set<String> successCodes = new HashSet<>(referenceCodes);
        successCodes.remove("IN_PROGRESS");
        assertThat(successCodes).hasSize(1);
        assertThat(transactionRepository.countByUserIdAndIdempotencyKey(userId, key)).isEqualTo(1);

        Wallet reloaded = walletRepository.findByUid(wallet.getUid()).orElseThrow();
        assertThat(reloaded.getBalance()).isEqualTo(101_000L);
    }

    @Test
    void withdraw_concurrent_sameKey_createsOneTransaction() throws Exception {
        String key = "wit-" + UUID.randomUUID();
        WithdrawRequest request = new WithdrawRequest();
        request.setWalletNumber(wallet.getWalletNumber());
        request.setAmount(1000L);
        request.setIdempotencyKey(key);

        List<String> referenceCodes = runConcurrently(10, () -> {
            authenticate(userId);
            try {
                TransferResponse response = withdrawService.withdraw(request).body().getData();
                return response.getReferenceCode();
            } catch (AppException e) {
                if (e.getErrorCode() == ErrorCode.IDEMPOTENCY_IN_PROGRESS) {
                    return "IN_PROGRESS";
                }
                throw e;
            }
        });

        Set<String> successCodes = new HashSet<>(referenceCodes);
        successCodes.remove("IN_PROGRESS");
        assertThat(successCodes).hasSize(1);
        assertThat(transactionRepository.countByUserIdAndIdempotencyKey(userId, key)).isEqualTo(1);

        Wallet reloaded = walletRepository.findByUid(wallet.getUid()).orElseThrow();
        assertThat(reloaded.getBalance()).isEqualTo(99_000L);
    }

    @Test
    void sameKey_differentUsers_areIsolated() {
        User other = createUser("idem2");
        Wallet otherWallet = createWallet(other, 0L);

        String key = "shared-" + UUID.randomUUID();

        authenticate(userId);
        String firstCode = depositService.deposit(depositRequest(wallet.getWalletNumber(), key)).body().getData().getReferenceCode();

        authenticate(other.getUid());
        String secondCode = depositService.deposit(depositRequest(otherWallet.getWalletNumber(), key)).body().getData().getReferenceCode();

        assertThat(firstCode).isNotEqualTo(secondCode);
        assertThat(transactionRepository.countByUserIdAndIdempotencyKey(userId, key)).isEqualTo(1);
        assertThat(transactionRepository.countByUserIdAndIdempotencyKey(other.getUid(), key)).isEqualTo(1);
    }

    private DepositRequest depositRequest(String walletNumber, String key) {
        DepositRequest request = new DepositRequest();
        request.setWalletNumber(walletNumber);
        request.setAmount(1000L);
        request.setIdempotencyKey(key);
        return request;
    }

    private List<String> runConcurrently(int threads, Callable<String> task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return task.call();
            }));
        }

        start.countDown();

        List<String> results = new ArrayList<>();
        for (Future<String> future : futures) {
            results.add(future.get(30, TimeUnit.SECONDS));
        }

        pool.shutdown();
        return results;
    }

    private void authenticate(UUID uid) {
        var authentication = new UsernamePasswordAuthenticationToken(
                uid.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User createUser(String prefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return userRepository.save(User.builder()
                .customerId(prefix.toUpperCase() + "-" + suffix)
                .fullName("Idem User")
                .email(prefix + "-" + suffix + "@example.com")
                .phone("08" + String.format("%08d", Math.abs(suffix.hashCode()) % 100000000))
                .password(passwordEncoder.encode("Password123"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .phoneVerified(true)
                .build());
    }

    private Wallet createWallet(User user, long balance) {
        return walletRepository.save(Wallet.builder()
                .user(user)
                .walletNumber(randomWalletNumber())
                .walletName(user.getFullName())
                .walletType(WalletType.CHECKING)
                .currency(CurrencyCode.VND)
                .balance(balance)
                .availableBalance(balance)
                .blockedBalance(0L)
                .status(WalletStatus.ACTIVE)
                .singleTransactionLimit(20_000_000L)
                .dailyLimit(50_000_000L)
                .monthlyLimit(100_000_000L)
                .openedAt(Instant.now())
                .build());
    }

    private String randomWalletNumber() {
        StringBuilder number = new StringBuilder("9");
        Random random = new Random();
        for (int i = 0; i < 11; i++) {
            number.append(random.nextInt(10));
        }
        return number.toString();
    }
}
