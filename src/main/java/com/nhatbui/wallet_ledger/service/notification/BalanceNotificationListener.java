package com.nhatbui.wallet_ledger.service.notification;

import com.nhatbui.wallet_ledger.event.BalanceChangedEvent;
import com.nhatbui.wallet_ledger.event.UserSessionRevokedEvent;
import com.nhatbui.wallet_ledger.websocket.WebSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceNotificationListener {

    private final BalanceNotificationPublisher publisher;
    private final WebSocketSessionRegistry sessionRegistry;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBalanceChanged(BalanceChangedEvent event) {
        try {
            publisher.publish(event);
        } catch (Exception e) {
            log.warn("Failed to push balance notification {}", event.eventId(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSessionRevoked(UserSessionRevokedEvent event) {
        try {
            if (event.tokenId() == null) {
                sessionRegistry.closeAllForUser(event.userId());
            } else {
                sessionRegistry.closeByTokenId(event.userId(), event.tokenId());
            }
        } catch (Exception e) {
            log.warn("Failed to close websocket sessions for user {}", event.userId(), e);
        }
    }
}
