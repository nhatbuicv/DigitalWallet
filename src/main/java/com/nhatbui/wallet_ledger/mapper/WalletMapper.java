package com.nhatbui.wallet_ledger.mapper;

import com.nhatbui.wallet_ledger.dto.response.admin.WalletAdminResponse;
import com.nhatbui.wallet_ledger.dto.response.wallet.BalanceResponse;
import com.nhatbui.wallet_ledger.dto.response.wallet.WalletResponse;
import com.nhatbui.wallet_ledger.dto.response.wallet.WalletSummaryResponse;
import com.nhatbui.wallet_ledger.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface WalletMapper {
    WalletResponse mapToWalletResponse(Wallet wallet);

    List<WalletSummaryResponse> mapToWalletSummaryResponse(List<Wallet> wallets);

    BalanceResponse mapToBalanceResponse(Wallet wallet);

    @Mapping(target = "userUid", source = "user.uid")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userPhone", source = "user.phone")
    @Mapping(target = "userEmail", source = "user.email")
    WalletAdminResponse mapToWalletAdminResponse(Wallet wallet);
}
