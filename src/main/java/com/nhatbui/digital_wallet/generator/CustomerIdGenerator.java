package com.nhatbui.digital_wallet.generator;

import com.nhatbui.digital_wallet.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerIdGenerator {

    private final JdbcTemplate jdbcTemplate;

    public String generateCustomerId(UserRole role) {
        Long nextVal = jdbcTemplate.queryForObject(
                "SELECT nextval('customer_id_seq')", Long.class);
        String prefix = switch (role) {
            case ADMIN -> "ADM";
            case STAFF -> "STF";
            case MANAGER -> "MGR";
            default -> "CUS";
        };
        return String.format("%s%08d", prefix, nextVal);
    }
}
