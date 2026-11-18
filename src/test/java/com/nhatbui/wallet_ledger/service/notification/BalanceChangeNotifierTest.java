package com.nhatbui.wallet_ledger.service.notification;

import com.nhatbui.wallet_ledger.entity.User;
import com.nhatbui.wallet_ledger.entity.Wallet;
import com.nhatbui.wallet_ledger.enums.EntryType;
import com.nhatbui.wallet_ledger.enums.TransactionType;
import com.nhatbui.wallet_ledger.event.BalanceChangedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class BalanceChangeNotifierTest {

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final BalanceChangeNotifier notifier = new BalanceChangeNotifier(publisher);

    @Test
    void publishesEventWithMaskedWalletNumberAndNewBalance() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUid(userId);
        Wallet wallet = Wallet.builder()
                .user(user)
                .walletNumber("123456789012")
                .balance(75_000L)
                .availableBalance(70_000L)
                .build();

        notifier.notifyBalanceChanged(wallet, EntryType.DEBIT, TransactionType.TRANSFER, 25_000L);

        ArgumentCaptor<BalanceChangedEvent> captor = ArgumentCaptor.forClass(BalanceChangedEvent.class);
        verify(publisher).publishEvent(captor.capture());
        BalanceChangedEvent event = captor.getValue();
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.maskedWalletNumber()).isEqualTo("****9012");
        assertThat(event.entryType()).isEqualTo(EntryType.DEBIT);
        assertThat(event.transactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(event.amount()).isEqualTo(25_000L);
        assertThat(event.balanceAfter()).isEqualTo(75_000L);
        assertThat(event.availableBalance()).isEqualTo(70_000L);
        assertThat(event.eventId()).isNotNull();
    }

    @Test
    void ignoresWalletWithoutOwner_systemWallet() {
        Wallet systemWallet = Wallet.builder().walletNumber("555555555555").build();

        notifier.notifyBalanceChanged(systemWallet, EntryType.CREDIT, TransactionType.TRANSFER, 1_000L);

        verify(publisher, never()).publishEvent(any(Object.class));
    }
}
