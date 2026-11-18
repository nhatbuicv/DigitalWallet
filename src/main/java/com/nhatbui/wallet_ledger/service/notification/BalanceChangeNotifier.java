package com.nhatbui.wallet_ledger.service.notification;

import com.nhatbui.wallet_ledger.entity.Wallet;
import com.nhatbui.wallet_ledger.enums.EntryType;
import com.nhatbui.wallet_ledger.enums.TransactionType;
import com.nhatbui.wallet_ledger.event.BalanceChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BalanceChangeNotifier {

    private final ApplicationEventPublisher eventPublisher;

    public void notifyBalanceChanged(Wallet wallet, EntryType entryType, TransactionType transactionType, Long amount) {
        if (wallet == null || wallet.getUser() == null) {
            return;
        }
        eventPublisher.publishEvent(new BalanceChangedEvent(
                UUID.randomUUID(),
                wallet.getUser().getUid(),
                mask(wallet.getWalletNumber()),
                entryType,
                transactionType,
                amount,
                wallet.getBalance(),
                wallet.getAvailableBalance(),
                Instant.now()));
    }

    static String mask(String walletNumber) {
        if (walletNumber == null || walletNumber.length() <= 4) {
            return walletNumber;
        }
        return "****" + walletNumber.substring(walletNumber.length() - 4);
    }
}
