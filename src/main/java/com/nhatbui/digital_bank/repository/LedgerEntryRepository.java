package com.nhatbui.digital_bank.repository;

import com.nhatbui.digital_bank.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
}
