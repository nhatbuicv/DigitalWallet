package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.entity.Transaction;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-24T20:18:37+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class TransferMapperImpl implements TransferMapper {

    @Override
    public TransferResponse mapToTransferResponse(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        TransferResponse.TransferResponseBuilder transferResponse = TransferResponse.builder();

        transferResponse.referenceCode( transaction.getReferenceCode() );
        transferResponse.amount( transaction.getAmount() );
        transferResponse.currency( transaction.getCurrency() );
        transferResponse.type( transaction.getType() );
        transferResponse.status( transaction.getStatus() );
        transferResponse.description( transaction.getDescription() );
        transferResponse.createdAt( transaction.getCreatedAt() );
        transferResponse.completedAt( transaction.getCompletedAt() );

        return transferResponse.build();
    }
}
