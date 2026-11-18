package com.nhatbui.wallet_ledger.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry of open mobile sockets (one user may have several devices).
 * Responsible for: per-user connection limit, safe concurrent sends, ping/pong keep-alive,
 * and closing sockets whose access token expired or whose session was revoked.
 */
@Component
@Slf4j
public class WebSocketSessionRegistry {

    private static final int SEND_TIME_LIMIT_MS = 5_000;
    private static final int SEND_BUFFER_LIMIT_BYTES = 64 * 1024;

    static final class Entry {
        final UUID userId;
        final WebSocketSession rawSession;
        final ConcurrentWebSocketSessionDecorator sender;
        final String tokenId;
        final Instant expiresAt;
        volatile Instant lastSeen = Instant.now();

        Entry(UUID userId, WebSocketSession rawSession, String tokenId, Instant expiresAt) {
            this.userId = userId;
            this.rawSession = rawSession;
            this.sender = new ConcurrentWebSocketSessionDecorator(rawSession, SEND_TIME_LIMIT_MS, SEND_BUFFER_LIMIT_BYTES);
            this.tokenId = tokenId;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<UUID, List<Entry>> sessions = new ConcurrentHashMap<>();
    private final int maxSessionsPerUser;
    private final Duration idleTimeout;

    public WebSocketSessionRegistry(
            @Value("${websocket.max-sessions-per-user:5}") int maxSessionsPerUser,
            @Value("${websocket.idle-timeout:90s}") Duration idleTimeout) {
        this.maxSessionsPerUser = maxSessionsPerUser;
        this.idleTimeout = idleTimeout;
    }

    public void register(UUID userId, WebSocketSession session, String tokenId, Instant expiresAt) {
        Entry entry = new Entry(userId, session, tokenId, expiresAt);
        List<Entry> evicted = new ArrayList<>();
        sessions.compute(userId, (id, current) -> {
            List<Entry> updated = current == null ? new ArrayList<>() : new ArrayList<>(current);
            updated.add(entry);
            while (updated.size() > maxSessionsPerUser) {
                evicted.add(updated.remove(0));
            }
            return updated;
        });
        evicted.forEach(e -> close(e, CloseStatus.POLICY_VIOLATION.withReason("Too many connections")));
    }

    public void unregister(UUID userId, WebSocketSession session) {
        remove(userId, e -> e.rawSession.getId().equals(session.getId()));
    }

    public void touch(UUID userId, WebSocketSession session) {
        forEachEntry(userId, e -> {
            if (e.rawSession.getId().equals(session.getId())) {
                e.lastSeen = Instant.now();
            }
        });
    }

    public int sessionCount(UUID userId) {
        List<Entry> list = sessions.get(userId);
        return list == null ? 0 : list.size();
    }

    public void sendToUser(UUID userId, String json) {
        List<Entry> list = sessions.get(userId);
        if (list == null) {
            return;
        }
        TextMessage message = new TextMessage(json);
        for (Entry entry : list) {
            if (!entry.sender.isOpen()) {
                remove(userId, e -> e == entry);
                continue;
            }
            try {
                entry.sender.sendMessage(message);
            } catch (Exception e) {
                log.warn("Send failed to websocket session {}, closing", entry.rawSession.getId(), e);
                close(entry, CloseStatus.SERVER_ERROR);
                remove(userId, x -> x == entry);
            }
        }
    }

    public void closeAllForUser(UUID userId) {
        List<Entry> removed = remove(userId, e -> true);
        removed.forEach(e -> close(e, CloseStatus.POLICY_VIOLATION.withReason("Session revoked")));
    }

    public void closeByTokenId(UUID userId, String tokenId) {
        List<Entry> removed = remove(userId, e -> tokenId.equals(e.tokenId));
        removed.forEach(e -> close(e, CloseStatus.POLICY_VIOLATION.withReason("Session revoked")));
    }

    @Scheduled(fixedDelayString = "${websocket.ping-interval-ms:30000}")
    public void sweep() {
        sweep(Instant.now());
    }

    void sweep(Instant now) {
        for (UUID userId : List.copyOf(sessions.keySet())) {
            List<Entry> dead = remove(userId, e ->
                    !e.sender.isOpen()
                            || (e.expiresAt != null && !e.expiresAt.isAfter(now))
                            || e.lastSeen.plus(idleTimeout).isBefore(now));
            dead.forEach(e -> close(e, CloseStatus.POLICY_VIOLATION.withReason("Token expired or idle")));
            forEachEntry(userId, e -> {
                try {
                    e.sender.sendMessage(new PingMessage());
                } catch (IOException | RuntimeException ex) {
                    log.debug("Ping failed for session {}", e.rawSession.getId(), ex);
                }
            });
        }
    }

    private void forEachEntry(UUID userId, java.util.function.Consumer<Entry> action) {
        List<Entry> list = sessions.get(userId);
        if (list != null) {
            list.forEach(action);
        }
    }

    private List<Entry> remove(UUID userId, java.util.function.Predicate<Entry> predicate) {
        List<Entry> removed = new ArrayList<>();
        sessions.computeIfPresent(userId, (id, current) -> {
            List<Entry> kept = new ArrayList<>();
            for (Entry e : current) {
                if (predicate.test(e)) {
                    removed.add(e);
                } else {
                    kept.add(e);
                }
            }
            return kept.isEmpty() ? null : kept;
        });
        return removed;
    }

    private void close(Entry entry, CloseStatus status) {
        try {
            entry.sender.close(status);
        } catch (Exception e) {
            log.debug("Error closing websocket session {}", entry.rawSession.getId(), e);
        }
    }
}
