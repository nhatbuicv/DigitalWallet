package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.transaction.LedgerEntryAdminResponse;
import com.nhatbui.digital_wallet.dto.response.transaction.TransactionAdminDetailResponse;
import com.nhatbui.digital_wallet.dto.response.transaction.TransactionAdminResponse;
import com.nhatbui.digital_wallet.entity.LedgerEntry;
import com.nhatbui.digital_wallet.entity.Transaction;
import com.nhatbui.digital_wallet.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "id", expression = "java(uuidToString(transaction.getId()))")
    @Mapping(target = "fromWalletId", expression = "java(walletId(transaction.getFromWallet()))")
    @Mapping(target = "fromWalletNumber", expression = "java(walletNumber(transaction.getFromWallet()))")
    @Mapping(target = "fromOwnerName", expression = "java(ownerName(transaction.getFromWallet()))")
    @Mapping(target = "toWalletId", expression = "java(walletId(transaction.getToWallet()))")
    @Mapping(target = "toWalletNumber", expression = "java(walletNumber(transaction.getToWallet()))")
    @Mapping(target = "toOwnerName", expression = "java(ownerName(transaction.getToWallet()))")
    TransactionAdminResponse mapToAdminResponse(Transaction transaction);

    @Mapping(target = "id", expression = "java(uuidToString(transaction.getId()))")
    @Mapping(target = "fromWalletId", expression = "java(walletId(transaction.getFromWallet()))")
    @Mapping(target = "fromWalletNumber", expression = "java(walletNumber(transaction.getFromWallet()))")
    @Mapping(target = "fromOwnerName", expression = "java(ownerName(transaction.getFromWallet()))")
    @Mapping(target = "toWalletId", expression = "java(walletId(transaction.getToWallet()))")
    @Mapping(target = "toWalletNumber", expression = "java(walletNumber(transaction.getToWallet()))")
    @Mapping(target = "toOwnerName", expression = "java(ownerName(transaction.getToWallet()))")
    @Mapping(target = "ledgerEntries", ignore = true)
    TransactionAdminDetailResponse mapToAdminDetailResponse(Transaction transaction);

    @Mapping(target = "id", expression = "java(uuidToString(entry.getId()))")
    @Mapping(target = "walletId", expression = "java(walletId(entry.getWallet()))")
    @Mapping(target = "walletNumber", expression = "java(walletNumber(entry.getWallet()))")
    LedgerEntryAdminResponse mapToLedgerEntryAdminResponse(LedgerEntry entry);

    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    default String walletId(Wallet wallet) {
        return wallet != null ? uuidToString(wallet.getUid()) : null;
    }

    default String walletNumber(Wallet wallet) {
        return wallet != null ? wallet.getWalletNumber() : null;
    }

    /**
     * Tên chủ ví. Ví hệ thống không có user nên dùng tên ví.
     */
    default String ownerName(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        if (wallet.getUser() != null) {
            return wallet.getUser().getFullName();
        }
        return wallet.getWalletName();
    }
}
