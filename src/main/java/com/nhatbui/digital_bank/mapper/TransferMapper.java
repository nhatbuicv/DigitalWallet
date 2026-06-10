package com.nhatbui.digital_bank.mapper;

import com.nhatbui.digital_bank.dto.response.TransferResponse;
import com.nhatbui.digital_bank.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferMapper {
    TransferResponse mapToTransferResponse(Transaction transaction);
}
