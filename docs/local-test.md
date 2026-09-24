1# Integration tests — chạy local với Testcontainers

> Task 01: nền test Postgres + Redis THẬT bằng Testcontainers + `@ServiceConnection` + JUnit 5.

## 1. Chuẩn bị (chỉ cần làm 1 lần)

1. Cài và **mở Docker Desktop** (engine phải ở trạng thái Running).
   Kiểm tra nhanh:
   ```bash
   docker ps
   ```
2. Không cần chuẩn bị database/Redis thủ công — Testcontainers tự kéo image
   (`postgres:17`, `redis:7`) và dựng container dùng một lần khi chạy test.
3. Không cần file `.env` cho integration test: profile `test`
   (`src/test/resources/application-test.yaml`) đã khai báo sẵn giá trị dummy cho
   `jwt` / `mail` / `esms`; datasource và Redis thật được `@ServiceConnection`
   bơm đè lên lúc runtime.

## 2. Cách viết test mới

Kế thừa `AbstractIntegrationTest` (package `com.nhatbui.digital_wallet`):

```java
class WalletIT extends AbstractIntegrationTest {

    @Autowired WalletRepository wallets;
    // test với Postgres + Redis thật, Flyway đã migrate V1...
}
```

- `@SpringBootTest` + `@ActiveProfiles("test")` nằm sẵn ở lớp cha.
- Containers là **singleton theo JVM** (static initializer): nhiều lớp test chia sẻ
  chung 1 Postgres + 1 Redis, tránh lỗi endpoint cũ khi Spring cache ApplicationContext
  giữa các lớp test, và tránh migrate Flyway lại từ đầu cho mỗi lớp.
- Redis dùng `GenericContainer` + `waitingFor("Ready to accept connections")` để test
  không chạy trước khi Redis sẵn sàng.

## 3. Lệnh chạy

```bash
# Smoke test Postgres + Redis (nhanh, nên chạy trước)
./mvnw test -Dtest=PostgresRedisSmokeTest

# Toàn bộ suite (unit + integration)
./mvnw test
```

> Windows dùng `mvnw.cmd`. Lần đầu chạy sẽ lâu (kéo image `postgres:17` ~400MB).

## 4. Quy ước version Postgres

- `compose.yaml` (môi trường dev/prod local): `postgres:14.23`.
- Testcontainers: **`postgres:17`** — vì `V1__init_schema.sql` có dòng
  `SET transaction_timeout = 0;` (do pg_dump từ PG17 sinh ra), PG14 không hiểu
  parameter này nên Flyway fail trên database trắng.
- **Không sửa V1**: migration đã release, sửa file sẽ lệch checksum Flyway trên
  các database hiện có (`validate-on-migrate=true` → app không boot được).
  Follow-up: khi nào nâng compose lên PG17 (hoặc dọn dòng SET + `flyway repair`
  có kiểm soát) thì pin lại test về cùng tag.

## 5. Troubleshooting (đã gặp thật trên máy dev, Windows + Docker Desktop 29.x)

| Triệu chứng | Nguyên nhân | Cách fix (đã áp dụng trong repo) |
|---|---|---|
| `Could not find a valid Docker environment` + `Status 400` ở `infoCmd` | docker-java (kèm Testcontainers) mặc định gọi Engine API **v1.32**, Docker Desktop 29.x trả `400 Bad Request` cho API cũ (đã probe: `/v1.32/info` → 400, `/v1.44/info` → 200) | `pom.xml` → surefire `<systemPropertyVariables><api.version>1.44</...>` (Testcontainers đã bump `1.19.3` → `1.21.3` qua property `testcontainers.version`) |
| `ERROR: unrecognized configuration parameter "transaction_timeout"` khi Flyway migrate trên container trắng | `V1` chứa parameter chỉ có từ PG17, container test ban đầu dùng PG14 | Đổi image test sang `postgres:17`, giữ nguyên V1 (xem mục 4) |
| Chạy lẻ từng lớp thì pass, chạy cả suite thì `Connection refused`/`Failed to obtain JDBC Connection` | Mỗi lớp test dựng container riêng (`@Container`) nhưng Spring tái dùng ApplicationContext đã cache → endpoint trỏ vào container đã stop | Chuyển sang singleton containers (static initializer) trong `AbstractIntegrationTest` |
| `DigitalWalletApplicationTests.contextLoads` báo `jdbcUrl, ${DB_URL}` | Test cũ không profile, phụ thuộc biến môi trường ngoài | Đã cho `extends AbstractIntegrationTest` → chạy trên container thật, không cần `.env` |
