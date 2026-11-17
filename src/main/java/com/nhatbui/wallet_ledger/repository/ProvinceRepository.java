package com.nhatbui.wallet_ledger.repository;

import com.nhatbui.wallet_ledger.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProvinceRepository extends JpaRepository<Province, UUID> {
    Optional<Province> findByCode(String code);
}
