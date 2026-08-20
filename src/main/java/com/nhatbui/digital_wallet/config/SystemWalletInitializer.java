package com.nhatbui.digital_wallet.config;

import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.generator.CustomerIdGenerator;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SystemWalletInitializer implements ApplicationRunner {
    public static final String SYSTEM_WALLET_NUMBER = "000000000000";
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerIdGenerator customerIdGenerator;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!walletRepository.existsByWalletNumber(SYSTEM_WALLET_NUMBER)) {
            Wallet systemWallet = Wallet.builder()
                    .walletNumber(SYSTEM_WALLET_NUMBER)
                    .walletName("System Wallet")
                    .walletType(WalletType.SYSTEM)
                    .status(WalletStatus.ACTIVE)
                    .balance(0L)
                    .build();
            walletRepository.save(systemWallet);
        }

        createUserIfNotExists("admin@wallet.local", "Admin@123", UserRole.ADMIN, "System Admin");
        createUserIfNotExists("staff@wallet.local", "Staff@123", UserRole.STAFF, "Bank Staff");
        createUserIfNotExists("manager@wallet.local", "Manager@123", UserRole.MANAGER, "Bank Manager");
    }

    private void createUserIfNotExists(String email, String password, UserRole role, String fullName) {
        if (userRepository.existsByEmail(email)) return;
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCustomerId(customerIdGenerator.generateCustomerId(role));
        userRepository.save(user);
    }
}
