package com.nhatbui.wallet_ledger.service.notification.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhatbui.wallet_ledger.dto.response.wallet.BalanceNotificationMessage;
import com.nhatbui.wallet_ledger.event.BalanceChangedEvent;
import com.nhatbui.wallet_ledger.service.notification.BalanceNotificationPublisher;
import com.nhatbui.wallet_ledger.websocket.WebSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalBalanceNotificationPublisher implements BalanceNotificationPublisher {

    private final WebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(BalanceChangedEvent event) {
        BalanceNotificationMessage message = new BalanceNotificationMessage(
                event.eventId(),
                BalanceNotificationMessage.BALANCE_CHANGED,
                event.maskedWalletNumber(),
                event.entryType(),
                event.transactionType(),
                event.amount(),
                event.balanceAfter(),
                event.availableBalance(),
                event.occurredAt());
        try {
            sessionRegistry.sendToUser(event.userId(), objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.warn("Cannot serialize balance notification {}", event.eventId(), e);
        }
    }
}
