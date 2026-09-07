package com.nhatbui.digital_wallet.dto.request.admin;

import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
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