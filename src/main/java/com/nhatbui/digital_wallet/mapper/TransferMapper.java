package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.TransferResponse;
import com.nhatbui.digital_wallet.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferMapper {
    TransferResponse mapToTransferResponse(Transaction transaction);
}
