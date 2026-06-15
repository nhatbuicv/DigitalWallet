package com.nhatbui.digital_wallet.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerIdGenerator {

    private final JdbcTemplate jdbcTemplate;

    public String generateCustomerId() {
        Long nextVal = jdbcTemplate.queryForObject(
                "SELECT nextval('customer_id_seq')", Long.class
        );
        return String.format("CUS%08d", nextVal);
    }
}
