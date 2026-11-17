package com.nhatbui.wallet_ledger.service.wallet;


import com.nhatbui.wallet_ledger.entity.Wallet;

public interface SystemWalletService {
    public Wallet getSystemWallet();

    public Wallet getSystemWalletWithLock();
}
