package com.nhatbui.digital_bank.repository;

import com.nhatbui.digital_bank.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProvinceRepository extends JpaRepository<Province, UUID> {
    Optional<Province> findByCode(String code);
}
