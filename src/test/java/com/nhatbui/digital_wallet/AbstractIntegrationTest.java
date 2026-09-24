package com.nhatbui.digital_wallet;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests that need REAL Postgres + Redis.
 *
 * <p>Requires Docker running locally (Testcontainers starts throwaway containers).
 *
 * <p>Containers are SINGLETONS started once per test JVM (static initializer, not
 * {@code @Container}): this keeps {@code @ServiceConnection} endpoints stable when
 * several test classes share the Spring TestContext cache, and avoids restarting
 * Postgres (Flyway migration) + Redis for every test class.</p>
 *
 * <p>Usage: {@code class MyIT extends AbstractIntegrationTest { ... }}</p>
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            // NOTE: tests run on PG17, not the compose.yaml PG14, because
            // V1__init_schema.sql contains "SET transaction_timeout" which only
            // exists on PG17+. V1 is left untouched on purpose (editing a released
            // migration would break Flyway checksum validation on existing DBs).
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"));

    @ServiceConnection(name = "redis")
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7"))
                    .withExposedPorts(6379)
                    .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));

    static {
        postgres.start();
        redis.start();
    }
}
