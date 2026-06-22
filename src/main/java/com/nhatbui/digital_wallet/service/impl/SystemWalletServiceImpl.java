package com.nhatbui.digital_wallet.service.impl;

import com.nhatbui.digital_wallet.config.SystemWalletInitializer;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.service.SystemWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemWalletServiceImpl implements SystemWalletService {

    private final WalletRepository walletRepository;

    @Override
    public Wallet getSystemWallet() {
        return walletRepository.findByWalletNumber(SystemWalletInitializer.SYSTEM_WALLET_NUMBER)
                .orElseThrow(() -> new AppException(ErrorCode.SYSTEM_WALLET_NOT_FOUND));
    }

    @Override
    public Wallet getSystemWalletWithLock() {
        return walletRepository.findByWalletNumberWithLock(SystemWalletInitializer.SYSTEM_WALLET_NUMBER)
                .orElseThrow(() -> new AppException(ErrorCode.SYSTEM_WALLET_NOT_FOUND));
    }
}
