package com.nhatbui.digital_bank.generator;

import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
