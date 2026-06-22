package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.BalanceResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletSummaryResponse;
import com.nhatbui.digital_wallet.entity.Wallet;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse mapToWalletResponse(Wallet wallet);

    List<WalletSummaryResponse> mapToWalletSummaryResponse(List<Wallet> wallets);

    BalanceResponse mapToBalanceResponse(Wallet wallet);
}
