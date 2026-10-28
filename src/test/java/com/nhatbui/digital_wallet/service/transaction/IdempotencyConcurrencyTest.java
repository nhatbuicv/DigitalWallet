package com.nhatbui.digital_wallet.service.transaction;

import com.nhatbui.digital_wallet.AbstractIntegrationTest;
import com.nhatbui.digital_wallet.dto.request.wallet.DepositRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.WithdrawRequest;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.repository.TransactionRepository;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

/**
 * Nhiều request song song cùng idempotency key chỉ tạo 1 giao dịch, các request
 * còn lại replay kết quả cũ. Key scope theo user nên 2 user dùng cùng key vẫn tách biệt.
 */
class IdempotencyConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private DepositService depositService;

    @Autowired
    private WithdrawService withdrawService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID userId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        User user = createUser("idem");
        userId = user.getUid();
        wallet = createWallet(user, 100_000L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
            TransferResponse response = depositService.deposit(request).getData();
            return response.getReferenceCode();
        });

        assertThat(new HashSet<>(referenceCodes)).hasSize(1);
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
            TransferResponse response = withdrawService.withdraw(request).getData();
            return response.getReferenceCode();
        });

        assertThat(new HashSet<>(referenceCodes)).hasSize(1);
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
        String firstCode = depositService.deposit(depositRequest(wallet.getWalletNumber(), key)).getData().getReferenceCode();

        authenticate(other.getUid());
        String secondCode = depositService.deposit(depositRequest(otherWallet.getWalletNumber(), key)).getData().getReferenceCode();

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
