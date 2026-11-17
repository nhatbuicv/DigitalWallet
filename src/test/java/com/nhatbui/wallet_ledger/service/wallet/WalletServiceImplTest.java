package com.nhatbui.wallet_ledger.service.wallet;

import com.nhatbui.wallet_ledger.config.SecurityUtils;
import com.nhatbui.wallet_ledger.config.WalletLimitProperties;
import com.nhatbui.wallet_ledger.dto.request.wallet.CreateWalletRequest;
import com.nhatbui.wallet_ledger.dto.response.wallet.WalletResponse;
import com.nhatbui.wallet_ledger.entity.User;
import com.nhatbui.wallet_ledger.entity.Wallet;
import com.nhatbui.wallet_ledger.enums.WalletType;
import com.nhatbui.wallet_ledger.generator.WalletNumberGenerator;
import com.nhatbui.wallet_ledger.mapper.WalletMapper;
import com.nhatbui.wallet_ledger.repository.UserRepository;
import com.nhatbui.wallet_ledger.repository.WalletRepository;
import com.nhatbui.wallet_ledger.service.wallet.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock private WalletNumberGenerator walletNumberGenerator;
    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private WalletMapper walletMapper;
    @Mock private SecurityUtils securityUtils;

    private WalletLimitProperties walletLimitProperties;
    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        walletLimitProperties = new WalletLimitProperties();
        walletService = new WalletServiceImpl(
                walletNumberGenerator,
                userRepository,
                walletRepository,
                walletMapper,
                securityUtils,
                walletLimitProperties);
    }

    @Test
    void createDefaultWallet_appliesConfiguredLimits() {
        User user = User.builder().fullName("Nguyen Van A").build();
        when(walletNumberGenerator.generate(null)).thenReturn("100000000001");

        Wallet wallet = walletService.createDefaultWallet(user);

        assertThat(wallet.getSingleTransactionLimit()).isEqualTo(20_000_000L);
        assertThat(wallet.getDailyLimit()).isEqualTo(50_000_000L);
        assertThat(wallet.getMonthlyLimit()).isEqualTo(100_000_000L);
    }

    @Test
    void createDefaultWallet_followsOverriddenConfiguration() {
        walletLimitProperties.setSingleTransactionLimit(10_000_000L);
        walletLimitProperties.setDailyLimit(30_000_000L);
        walletLimitProperties.setMonthlyLimit(60_000_000L);
        User user = User.builder().fullName("Nguyen Van B").build();
        when(walletNumberGenerator.generate(null)).thenReturn("100000000002");

        Wallet wallet = walletService.createDefaultWallet(user);

        assertThat(wallet.getSingleTransactionLimit()).isEqualTo(10_000_000L);
        assertThat(wallet.getDailyLimit()).isEqualTo(30_000_000L);
        assertThat(wallet.getMonthlyLimit()).isEqualTo(60_000_000L);
    }

    @Test
    void createWallet_appliesConfiguredLimits() {
        User user = User.builder().fullName("Nguyen Van C").build();
        CreateWalletRequest request = new CreateWalletRequest();
        request.setWalletType(WalletType.MERCHANT);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(walletNumberGenerator.generate(WalletType.MERCHANT)).thenReturn("200000000001");
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletMapper.mapToWalletResponse(any(Wallet.class))).thenReturn(WalletResponse.builder().build());

        walletService.createWallet(request);

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(captor.capture());
        Wallet saved = captor.getValue();

        assertThat(saved.getWalletType()).isEqualTo(WalletType.MERCHANT);
        assertThat(saved.getSingleTransactionLimit()).isEqualTo(20_000_000L);
        assertThat(saved.getDailyLimit()).isEqualTo(50_000_000L);
        assertThat(saved.getMonthlyLimit()).isEqualTo(100_000_000L);
    }
}
