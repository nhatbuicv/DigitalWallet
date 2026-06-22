package com.nhatbui.digital_wallet.service;


import com.nhatbui.digital_wallet.entity.Wallet;

public interface SystemWalletService {
    public Wallet getSystemWallet();

    public Wallet getSystemWalletWithLock();
}
