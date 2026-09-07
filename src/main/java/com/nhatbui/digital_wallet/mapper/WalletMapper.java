package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.admin.WalletAdminResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.BalanceResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletSummaryResponse;
import com.nhatbui.digital_wallet.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse mapToWalletResponse(Wallet wallet);

    List<WalletSummaryResponse> mapToWalletSummaryResponse(List<Wallet> wallets);

    BalanceResponse mapToBalanceResponse(Wallet wallet);

    @Mapping(target = "uid", expression = "java(wallet.getUid() != null ? wallet.getUid().toString() : null)")
    @Mapping(target = "userUid", expression = "java(wallet.getUser() != null ? wallet.getUser().getUid().toString() : null)")
    @Mapping(target = "userFullName", expression = "java(wallet.getUser() != null ? wallet.getUser().getFullName() : null)")
    @Mapping(target = "userPhone", expression = "java(wallet.getUser() != null ? wallet.getUser().getPhone() : null)")
    @Mapping(target = "userEmail", expression = "java(wallet.getUser() != null ? wallet.getUser().getEmail() : null)")
    WalletAdminResponse mapToWalletAdminResponse(Wallet wallet);
}
