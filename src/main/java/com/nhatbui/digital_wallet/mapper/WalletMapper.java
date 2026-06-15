package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.BalanceResponse;
import com.nhatbui.digital_wallet.dto.response.account.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.account.WalletSummaryResponse;
import com.nhatbui.digital_wallet.entity.Wallet;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse mapToAccountResponse(Wallet wallet);

    List<WalletSummaryResponse> mapToAccountSummaryResponse(List<Wallet> wallets);

    BalanceResponse mapToBalanceResponse(Wallet wallet);
}
