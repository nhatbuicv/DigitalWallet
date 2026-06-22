package com.nhatbui.digital_wallet.config;

import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SystemWalletInitializer implements ApplicationRunner {
    public static final String SYSTEM_WALLET_NUMBER = "000000000000";
    private final WalletRepository walletRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!walletRepository.existsByWalletNumber(SYSTEM_WALLET_NUMBER)) {
            Wallet systemWallet = Wallet.builder()
                    .walletNumber(SYSTEM_WALLET_NUMBER)
                    .walletName("System Wallet")
                    .walletType(WalletType.SYSTEM)
                    .status(WalletStatus.ACTIVE)
                    .balance(BigDecimal.ZERO)
                    .build();
            walletRepository.save(systemWallet);
        }
    }
}
