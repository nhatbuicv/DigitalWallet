package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProvinceRepository extends JpaRepository<Province, UUID> {
    Optional<Province> findByCode(String code);
}
