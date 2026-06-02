# 🚀 Digital Banking System Backend

> **Backend API cho hệ thống ngân hàng số và ví điện tử, tập trung vào xử lý giao dịch, bảo mật và transaction consistency.**

---

## 🛠 1. Công nghệ sử dụng

| Thành phần | Công nghệ                                  |
| :--- |:-------------------------------------------|
| **Ngôn ngữ** | Java 21                                    |
| **Framework** | Spring Boot 3                              |
| **Cơ sở dữ liệu** | PostgreSQL 14+                             |
| **Bảo mật** | JWT (Nimbus JOSE + Spring Resource Server) |
| **API Docs** | springdoc-openapi (Swagger UI)             |
| **Tiện ích** | MapStruct, Lombok, Maven                   |

---

## 💻 2. Yêu cầu môi trường

- **JDK:** 21
- **Maven:** 3.9+ (hoặc dùng `./mvnw`)
- **PostgreSQL:** 14+
- **Docker** (tuỳ chọn, để chạy DB nhanh)

---

## 🚀 3. Hướng dẫn chạy dự án



1. **Khởi động PostgreSQL:**
   ```bash
   docker compose up -d
   ```

2. **Chạy ứng dụng:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Truy cập ứng dụng:**
    - API base URL: `http://localhost:8080`
    - Swagger UI: `http://localhost:8080/swagger-ui/index.html`


---