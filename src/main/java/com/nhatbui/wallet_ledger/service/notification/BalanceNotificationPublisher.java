package com.nhatbui.wallet_ledger.service.notification;

import com.nhatbui.wallet_ledger.event.BalanceChangedEvent;

public interface BalanceNotificationPublisher {
    void publish(BalanceChangedEvent event);
}
