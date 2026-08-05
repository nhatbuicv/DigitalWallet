package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.enums.UserRole;
import com.nhatbui.digital_wallet.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByUid(UUID uid);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByStatusAndRole(UserStatus status, UserRole role, Pageable pageable);
}