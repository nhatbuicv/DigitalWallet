package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.wallet.TransactionDetailResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionHistoryResponse;
import com.nhatbui.digital_wallet.entity.LedgerEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
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
}
