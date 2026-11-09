package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.admin.WalletAdminResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.BalanceResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletSummaryResponse;
import com.nhatbui.digital_wallet.entity.Wallet;
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
