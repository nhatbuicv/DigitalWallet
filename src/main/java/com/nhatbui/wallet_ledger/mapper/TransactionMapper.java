package com.nhatbui.wallet_ledger.mapper;

import com.nhatbui.wallet_ledger.dto.response.transaction.LedgerEntryAdminResponse;
import com.nhatbui.wallet_ledger.dto.response.transaction.TransactionAdminDetailResponse;
import com.nhatbui.wallet_ledger.dto.response.transaction.TransactionAdminResponse;
import com.nhatbui.wallet_ledger.dto.response.wallet.TransactionDetailResponse;
import com.nhatbui.wallet_ledger.dto.response.wallet.TransactionHistoryResponse;
import com.nhatbui.wallet_ledger.entity.LedgerEntry;
import com.nhatbui.wallet_ledger.entity.Transaction;
import com.nhatbui.wallet_ledger.repository.projection.TransactionAdminProjection;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TransactionMapper {
    @Mapping(target = ".", source = "transaction")
    @Mapping(target = "transactionId", source = "transaction.id")
    TransactionHistoryResponse toHistoryResponse(LedgerEntry entry);

    @Mapping(target = ".", source = "transaction")
    @Mapping(target = "transactionId", source = "transaction.id")
    @Mapping(target = "fromWalletNumber", source = "transaction.fromWallet.walletNumber")
    @Mapping(target = "fromOwnerName", source = "transaction.fromWallet.user.fullName")
    @Mapping(target = "toWalletNumber", source = "transaction.toWallet.walletNumber")
    @Mapping(target = "toOwnerName", source = "transaction.toWallet.user.fullName")
    TransactionDetailResponse toDetailResponse(LedgerEntry entry);

    @Mapping(target = "fromWalletId", source = "fromWallet.uid")
    @Mapping(target = "fromWalletNumber", source = "fromWallet.walletNumber")
    @Mapping(target = "fromOwnerName", source = "fromWallet.user.fullName")
    @Mapping(target = "toWalletId", source = "toWallet.uid")
    @Mapping(target = "toWalletNumber", source = "toWallet.walletNumber")
    @Mapping(target = "toOwnerName", source = "toWallet.user.fullName")
    TransactionAdminResponse mapToAdminResponse(Transaction transaction);

    TransactionAdminResponse mapToAdminResponse(TransactionAdminProjection projection);

    @InheritConfiguration(name = "mapToAdminResponse")
    TransactionAdminDetailResponse mapToAdminDetailResponse(Transaction transaction);

    @Mapping(target = "walletId", source = "wallet.uid")
    @Mapping(target = "walletNumber", source = "wallet.walletNumber")
    LedgerEntryAdminResponse mapToLedgerEntryAdminResponse(LedgerEntry entry);
}
