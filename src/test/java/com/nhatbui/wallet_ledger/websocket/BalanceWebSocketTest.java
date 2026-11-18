package com.nhatbui.wallet_ledger.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhatbui.wallet_ledger.AbstractIntegrationTest;
import com.nhatbui.wallet_ledger.entity.User;
import com.nhatbui.wallet_ledger.enums.EntryType;
import com.nhatbui.wallet_ledger.enums.TransactionType;
import com.nhatbui.wallet_ledger.enums.UserRole;
import com.nhatbui.wallet_ledger.enums.UserStatus;
import com.nhatbui.wallet_ledger.event.BalanceChangedEvent;
import com.nhatbui.wallet_ledger.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end test of the one-way balance WebSocket against REAL Postgres + Redis (Testcontainers).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BalanceWebSocketTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ApplicationEventPublisher eventPublisher;
    @Autowired private TransactionTemplate transactionTemplate;

    private final StandardWebSocketClient client = new StandardWebSocketClient();
    private String email;
    private User user;
    private WebSocketSession openSession;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        email = "ws-" + suffix + "@example.com";
        user = userRepository.save(User.builder()
                .customerId("WS-" + suffix)
                .fullName("Ws User")
                .email(email)
                .phone("09" + String.format("%08d", Math.abs(suffix.hashCode()) % 100000000))
                .password(passwordEncoder.encode("Password123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .phoneVerified(true)
                .failedLoginAttempts(0)
                .walletNonLocked(true)
                .tokenVersion(0)
                .build());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (openSession != null && openSession.isOpen()) {
            openSession.close();
        }
    }

    @Test
    void handshakeWithoutToken_isRejected() {
        assertThatThrownBy(() -> client.execute(new Collector(), new WebSocketHttpHeaders(), uri()).get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class);
    }

    @Test
    void handshakeWithInvalidToken_isRejected() {
        assertThatThrownBy(() -> connect("not-a-jwt", new Collector()))
                .isInstanceOf(ExecutionException.class);
    }

    @Test
    void committedBalanceChange_isPushedToOwner() throws Exception {
        Tokens tokens = login("dev-commit");
        Collector collector = new Collector();
        openSession = connect(tokens.accessToken(), collector);

        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(event(user.getUid(), 75_000L)));

        String json = collector.messages.poll(5, TimeUnit.SECONDS);
        assertThat(json).isNotNull();
        JsonNode node = objectMapper.readTree(json);
        assertThat(node.path("type").asText()).isEqualTo("BALANCE_CHANGED");
        assertThat(node.path("balanceAfter").asLong()).isEqualTo(75_000L);
        assertThat(node.path("walletNumber").asText()).isEqualTo("****9012");
        assertThat(node.has("description")).isFalse();
    }

    @Test
    void rolledBackTransaction_pushesNothing() throws Exception {
        Tokens tokens = login("dev-rollback");
        Collector collector = new Collector();
        openSession = connect(tokens.accessToken(), collector);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.publishEvent(event(user.getUid(), 1L));
            throw new IllegalStateException("rollback");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(collector.messages.poll(1500, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void otherUsersDoNotReceiveMessages() throws Exception {
        Tokens tokens = login("dev-isolation");
        Collector collector = new Collector();
        openSession = connect(tokens.accessToken(), collector);

        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(event(UUID.randomUUID(), 5L)));

        assertThat(collector.messages.poll(1500, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void logout_closesTheSocket() throws Exception {
        Tokens tokens = login("dev-logout");
        Collector collector = new Collector();
        openSession = connect(tokens.accessToken(), collector);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.accessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        var response = restTemplate.postForEntity("/auth/logout",
                new HttpEntity<>("{\"refreshToken\":\"" + tokens.refreshToken() + "\"}", headers), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        CloseStatus status = collector.closed.get(5, TimeUnit.SECONDS);
        assertThat(status).isNotNull();
    }

    private BalanceChangedEvent event(UUID userId, long balanceAfter) {
        return new BalanceChangedEvent(UUID.randomUUID(), userId, "****9012", EntryType.CREDIT,
                TransactionType.DEPOSIT, 1_000L, balanceAfter, balanceAfter, Instant.now());
    }

    private URI uri() {
        return URI.create("ws://localhost:" + port + "/ws/balance");
    }

    private WebSocketSession connect(String accessToken, Collector collector) throws Exception {
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setBearerAuth(accessToken);
        return client.execute(collector, headers, uri()).get(5, TimeUnit.SECONDS);
    }

    private Tokens login(String deviceId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"identifier\":\"" + email + "\",\"clientType\":\"WEB_ADMIN\","
                + "\"password\":\"Password123\",\"deviceId\":\"" + deviceId + "\","
                + "\"deviceName\":\"integration-test\"}";
        String response = restTemplate.postForObject("/auth/login", new HttpEntity<>(body, headers), String.class);
        JsonNode data = objectMapper.readTree(response).path("data");
        return new Tokens(data.path("accessToken").asText(), data.path("refreshToken").asText());
    }

    private record Tokens(String accessToken, String refreshToken) {
    }

    private static class Collector extends TextWebSocketHandler {
        final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        final CompletableFuture<CloseStatus> closed = new CompletableFuture<>();

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            messages.add(message.getPayload());
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            closed.complete(status);
        }
    }
}
