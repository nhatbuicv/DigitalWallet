package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface TransferMapper {
    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "fromWalletId", source = "fromWallet.uid")
    @Mapping(target = "toWalletId", source = "toWallet.uid")
    TransferResponse mapToTransferResponse(Transaction transaction);
}
