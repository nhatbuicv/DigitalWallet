package com.nhatbui.wallet_ledger.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.security.Principal;
import java.util.UUID;

/**
 * One-way channel: server -> mobile. Anything the client sends (other than pong frames) is ignored.
 * Authentication already happened in the Spring Security filter chain during the HTTP upgrade.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceWebSocketHandler extends TextWebSocketHandler {

    private static final int MAX_INCOMING_MESSAGE_BYTES = 1024;

    private final WebSocketSessionRegistry registry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = userIdOf(session);
        Principal principal = session.getPrincipal();
        if (userId == null || !(principal instanceof JwtAuthenticationToken jwtAuth)) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Unauthenticated"));
            return;
        }
        session.setTextMessageSizeLimit(MAX_INCOMING_MESSAGE_BYTES);
        session.setBinaryMessageSizeLimit(MAX_INCOMING_MESSAGE_BYTES);
        Jwt jwt = jwtAuth.getToken();
        registry.register(userId, session, jwt.getId(), jwt.getExpiresAt());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // one-way: ignore client messages
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) {
        UUID userId = userIdOf(session);
        if (userId != null) {
            registry.touch(userId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID userId = userIdOf(session);
        if (userId != null) {
            registry.unregister(userId, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.debug("Websocket transport error on session {}", session.getId(), exception);
        UUID userId = userIdOf(session);
        if (userId != null) {
            registry.unregister(userId, session);
        }
    }

    private UUID userIdOf(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal == null) {
            return null;
        }
        try {
            return UUID.fromString(principal.getName());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
