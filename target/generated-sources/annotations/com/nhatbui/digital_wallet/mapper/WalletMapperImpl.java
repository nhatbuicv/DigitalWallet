package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.admin.WalletAdminResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.BalanceResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletSummaryResponse;
import com.nhatbui.digital_wallet.entity.Wallet;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-24T20:18:37+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class WalletMapperImpl implements WalletMapper {

    @Override
    public WalletResponse mapToWalletResponse(Wallet wallet) {
        if ( wallet == null ) {
            return null;
        }

        WalletResponse.WalletResponseBuilder walletResponse = WalletResponse.builder();

        walletResponse.uid( wallet.getUid() );
        walletResponse.walletNumber( wallet.getWalletNumber() );
        walletResponse.walletName( wallet.getWalletName() );
        walletResponse.walletType( wallet.getWalletType() );
        walletResponse.currency( wallet.getCurrency() );
        walletResponse.balance( wallet.getBalance() );
        walletResponse.availableBalance( wallet.getAvailableBalance() );
        walletResponse.blockedBalance( wallet.getBlockedBalance() );
        walletResponse.status( wallet.getStatus() );
        walletResponse.branchCode( wallet.getBranchCode() );
        walletResponse.branchName( wallet.getBranchName() );
        walletResponse.dailyLimit( wallet.getDailyLimit() );
        walletResponse.monthlyLimit( wallet.getMonthlyLimit() );
        walletResponse.openedAt( wallet.getOpenedAt() );
        walletResponse.createdAt( wallet.getCreatedAt() );

        return walletResponse.build();
    }

    @Override
    public List<WalletSummaryResponse> mapToWalletSummaryResponse(List<Wallet> wallets) {
        if ( wallets == null ) {
            return null;
        }

        List<WalletSummaryResponse> list = new ArrayList<WalletSummaryResponse>( wallets.size() );
        for ( Wallet wallet : wallets ) {
            list.add( walletToWalletSummaryResponse( wallet ) );
        }

        return list;
    }

    @Override
    public BalanceResponse mapToBalanceResponse(Wallet wallet) {
        if ( wallet == null ) {
            return null;
        }

        BalanceResponse.BalanceResponseBuilder balanceResponse = BalanceResponse.builder();

        balanceResponse.walletNumber( wallet.getWalletNumber() );
        balanceResponse.currency( wallet.getCurrency() );
        balanceResponse.balance( wallet.getBalance() );
        balanceResponse.availableBalance( wallet.getAvailableBalance() );
        balanceResponse.blockedBalance( wallet.getBlockedBalance() );

        return balanceResponse.build();
    }

    @Override
    public WalletAdminResponse mapToWalletAdminResponse(Wallet wallet) {
        if ( wallet == null ) {
            return null;
        }

        WalletAdminResponse.WalletAdminResponseBuilder walletAdminResponse = WalletAdminResponse.builder();

        walletAdminResponse.walletNumber( wallet.getWalletNumber() );
        walletAdminResponse.walletName( wallet.getWalletName() );
        if ( wallet.getWalletType() != null ) {
            walletAdminResponse.walletType( wallet.getWalletType().name() );
        }
        if ( wallet.getCurrency() != null ) {
            walletAdminResponse.currency( wallet.getCurrency().name() );
        }
        walletAdminResponse.balance( wallet.getBalance() );
        walletAdminResponse.availableBalance( wallet.getAvailableBalance() );
        walletAdminResponse.blockedBalance( wallet.getBlockedBalance() );
        walletAdminResponse.overdraftLimit( wallet.getOverdraftLimit() );
        walletAdminResponse.minimumBalance( wallet.getMinimumBalance() );
        walletAdminResponse.dailyLimit( wallet.getDailyLimit() );
        walletAdminResponse.monthlyLimit( wallet.getMonthlyLimit() );
        walletAdminResponse.singleTransactionLimit( wallet.getSingleTransactionLimit() );
        if ( wallet.getStatus() != null ) {
            walletAdminResponse.status( wallet.getStatus().name() );
        }
        walletAdminResponse.branchCode( wallet.getBranchCode() );
        walletAdminResponse.branchName( wallet.getBranchName() );
        walletAdminResponse.openedAt( wallet.getOpenedAt() );
        walletAdminResponse.closedAt( wallet.getClosedAt() );
        walletAdminResponse.closedReason( wallet.getClosedReason() );
        walletAdminResponse.createdAt( wallet.getCreatedAt() );
        walletAdminResponse.updatedAt( wallet.getUpdatedAt() );
        walletAdminResponse.version( wallet.getVersion() );

        walletAdminResponse.uid( wallet.getUid() != null ? wallet.getUid().toString() : null );
        walletAdminResponse.userUid( wallet.getUser() != null ? wallet.getUser().getUid().toString() : null );
        walletAdminResponse.userFullName( wallet.getUser() != null ? wallet.getUser().getFullName() : null );
        walletAdminResponse.userPhone( wallet.getUser() != null ? wallet.getUser().getPhone() : null );
        walletAdminResponse.userEmail( wallet.getUser() != null ? wallet.getUser().getEmail() : null );

        return walletAdminResponse.build();
    }

    protected WalletSummaryResponse walletToWalletSummaryResponse(Wallet wallet) {
        if ( wallet == null ) {
            return null;
        }

        WalletSummaryResponse.WalletSummaryResponseBuilder walletSummaryResponse = WalletSummaryResponse.builder();

        walletSummaryResponse.uid( wallet.getUid() );
        walletSummaryResponse.walletNumber( wallet.getWalletNumber() );
        walletSummaryResponse.walletName( wallet.getWalletName() );
        walletSummaryResponse.walletType( wallet.getWalletType() );
        walletSummaryResponse.currency( wallet.getCurrency() );
        walletSummaryResponse.balance( wallet.getBalance() );
        walletSummaryResponse.status( wallet.getStatus() );

        return walletSummaryResponse.build();
    }
}
