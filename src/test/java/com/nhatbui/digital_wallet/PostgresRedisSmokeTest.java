package com.nhatbui.digital_wallet;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Smoke test: proves Testcontainers Postgres + Redis are up and wired via
 * {@code @ServiceConnection} (runs Flyway migration + real Redis round-trip).
 */
class PostgresRedisSmokeTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void postgres_shouldBeReachableAndMigrated() {
        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertThat(one).isEqualTo(1);

        // Flyway V1 creates the core schema — proves migrations ran on the container.
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN ('users','wallets','transactions')",
                Integer.class);
        assertThat(tableCount).isEqualTo(3);
    }

    @Test
    void redis_shouldRoundTrip() {
        redisTemplate.opsForValue().set("it:smoke", "ok");
        assertThat(redisTemplate.opsForValue().get("it:smoke")).isEqualTo("ok");
        redisTemplate.delete("it:smoke");
    }
}
