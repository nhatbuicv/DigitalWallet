package com.nhatbui.wallet_ledger.mapper;

import com.nhatbui.wallet_ledger.dto.response.wallet.TransferResponse;
import com.nhatbui.wallet_ledger.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TransferMapper {
    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "fromWalletId", source = "fromWallet.uid")
    @Mapping(target = "toWalletId", source = "toWallet.uid")
    TransferResponse mapToTransferResponse(Transaction transaction);
}
