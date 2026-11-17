package com.nhatbui.wallet_ledger.service.wallet.impl;

import com.nhatbui.wallet_ledger.config.SystemWalletInitializer;
import com.nhatbui.wallet_ledger.entity.Wallet;
import com.nhatbui.wallet_ledger.exception.AppException;
import com.nhatbui.wallet_ledger.exception.ErrorCode;
import com.nhatbui.wallet_ledger.repository.WalletRepository;
import com.nhatbui.wallet_ledger.service.wallet.SystemWalletService;
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
