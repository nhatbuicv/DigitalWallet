package com.nhatbui.digital_wallet.repository;

import com.nhatbui.digital_wallet.entity.WalletStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletStatusHistoryRepository extends JpaRepository<WalletStatusHistory, UUID> {
}