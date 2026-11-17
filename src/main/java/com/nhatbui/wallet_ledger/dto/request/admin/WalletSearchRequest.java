package com.nhatbui.wallet_ledger.dto.request.admin;

import com.nhatbui.wallet_ledger.enums.WalletStatus;
import com.nhatbui.wallet_ledger.enums.WalletType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletSearchRequest {
    private String walletNumber;
    private String userPhone;
    private String userEmail;
    private String userName;
    private WalletStatus status;
    private WalletType walletType;
}