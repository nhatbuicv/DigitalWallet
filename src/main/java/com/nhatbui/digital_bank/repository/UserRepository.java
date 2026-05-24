package com.nhatbui.digital_bank.repository;

import com.nhatbui.digital_bank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findTopByOrderByUidDesc();

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
