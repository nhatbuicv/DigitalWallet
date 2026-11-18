package com.nhatbui.wallet_ledger.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebSocketSessionRegistryTest {

    private static final UUID USER_A = UUID.randomUUID();
    private static final UUID USER_B = UUID.randomUUID();
    private static final Instant FAR_FUTURE = Instant.now().plus(Duration.ofHours(1));

    private WebSocketSessionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new WebSocketSessionRegistry(2, Duration.ofSeconds(90));
    }

    private WebSocketSession openSession(String id) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    private static boolean isText(WebSocketMessage<?> message, String payload) {
        return message instanceof TextMessage text && payload.equals(text.getPayload());
    }

    @Test
    void sendToUser_deliversToAllDevicesOfThatUserOnly() throws IOException {
        WebSocketSession phone = openSession("phone");
        WebSocketSession tablet = openSession("tablet");
        WebSocketSession other = openSession("other");
        registry.register(USER_A, phone, "jti-1", FAR_FUTURE);
        registry.register(USER_A, tablet, "jti-2", FAR_FUTURE);
        registry.register(USER_B, other, "jti-3", FAR_FUTURE);

        registry.sendToUser(USER_A, "{\"x\":1}");

        verify(phone).sendMessage(argThat(m -> isText(m, "{\"x\":1}")));
        verify(tablet).sendMessage(argThat(m -> isText(m, "{\"x\":1}")));
        verify(other, never()).sendMessage(any());
    }

    @Test
    void sendToUser_unknownUser_isNoOp() {
        registry.sendToUser(UUID.randomUUID(), "{}");
    }

    @Test
    void register_overLimit_evictsOldestSession() throws IOException {
        WebSocketSession first = openSession("s1");
        WebSocketSession second = openSession("s2");
        WebSocketSession third = openSession("s3");
        registry.register(USER_A, first, "j1", FAR_FUTURE);
        registry.register(USER_A, second, "j2", FAR_FUTURE);
        registry.register(USER_A, third, "j3", FAR_FUTURE);

        assertThat(registry.sessionCount(USER_A)).isEqualTo(2);
        verify(first).close(any(CloseStatus.class));
        verify(second, never()).close(any(CloseStatus.class));
    }

    @Test
    void unregister_removesSession() {
        WebSocketSession session = openSession("s1");
        registry.register(USER_A, session, "j1", FAR_FUTURE);

        registry.unregister(USER_A, session);

        assertThat(registry.sessionCount(USER_A)).isZero();
    }

    @Test
    void closeByTokenId_closesOnlyMatchingToken() throws IOException {
        WebSocketSession revoked = openSession("s1");
        WebSocketSession kept = openSession("s2");
        registry.register(USER_A, revoked, "revoked-jti", FAR_FUTURE);
        registry.register(USER_A, kept, "other-jti", FAR_FUTURE);

        registry.closeByTokenId(USER_A, "revoked-jti");

        verify(revoked).close(any(CloseStatus.class));
        verify(kept, never()).close(any(CloseStatus.class));
        assertThat(registry.sessionCount(USER_A)).isEqualTo(1);
    }

    @Test
    void closeAllForUser_closesEverySession() throws IOException {
        WebSocketSession s1 = openSession("s1");
        WebSocketSession s2 = openSession("s2");
        registry.register(USER_A, s1, "j1", FAR_FUTURE);
        registry.register(USER_A, s2, "j2", FAR_FUTURE);

        registry.closeAllForUser(USER_A);

        verify(s1).close(any(CloseStatus.class));
        verify(s2).close(any(CloseStatus.class));
        assertThat(registry.sessionCount(USER_A)).isZero();
    }

    @Test
    void sweep_closesSessionsWithExpiredToken() throws IOException {
        WebSocketSession expired = openSession("s1");
        WebSocketSession valid = openSession("s2");
        registry.register(USER_A, expired, "j1", Instant.now().minusSeconds(1));
        registry.register(USER_A, valid, "j2", FAR_FUTURE);

        registry.sweep(Instant.now());

        verify(expired).close(any(CloseStatus.class));
        verify(valid, never()).close(any(CloseStatus.class));
        verify(valid).sendMessage(any(PingMessage.class));
        assertThat(registry.sessionCount(USER_A)).isEqualTo(1);
    }

    @Test
    void sweep_closesIdleSessionsWithoutPong() throws IOException {
        WebSocketSession idle = openSession("s1");
        registry.register(USER_A, idle, "j1", FAR_FUTURE);

        registry.sweep(Instant.now().plus(Duration.ofMinutes(5)));

        verify(idle).close(any(CloseStatus.class));
        assertThat(registry.sessionCount(USER_A)).isZero();
    }

    @Test
    void touch_keepsSessionAliveAcrossSweeps() throws IOException {
        WebSocketSession session = openSession("s1");
        registry.register(USER_A, session, "j1", FAR_FUTURE);

        registry.touch(USER_A, session);
        registry.sweep(Instant.now().plusSeconds(30));

        verify(session, never()).close(any(CloseStatus.class));
        assertThat(registry.sessionCount(USER_A)).isEqualTo(1);
    }

    @Test
    void sendToUser_failureClosesAndRemovesSession() throws IOException {
        WebSocketSession broken = openSession("s1");
        org.mockito.Mockito.doThrow(new IOException("boom")).when(broken).sendMessage(any());
        registry.register(USER_A, broken, "j1", FAR_FUTURE);

        registry.sendToUser(USER_A, "{}");

        assertThat(registry.sessionCount(USER_A)).isZero();
    }
}
