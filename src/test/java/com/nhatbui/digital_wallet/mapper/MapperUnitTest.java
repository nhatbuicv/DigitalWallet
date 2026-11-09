package com.nhatbui.digital_wallet.mapper;

import com.nhatbui.digital_wallet.dto.response.admin.UserAdminResponse;
import com.nhatbui.digital_wallet.dto.response.admin.WalletAdminResponse;
import com.nhatbui.digital_wallet.dto.response.user.UserProfileResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.BalanceResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionDetailResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransactionHistoryResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletResponse;
import com.nhatbui.digital_wallet.entity.LedgerEntry;
import com.nhatbui.digital_wallet.entity.Province;
import com.nhatbui.digital_wallet.entity.Transaction;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.enums.EntryType;
import com.nhatbui.digital_wallet.enums.TransactionStatus;
import com.nhatbui.digital_wallet.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MapperUnitTest {

    private ProvinceMapper provinceMapper;
    private UserMapper userMapper;
    private WalletMapper walletMapper;
    private TransactionMapper transactionMapper;
    private TransferMapper transferMapper;

    @BeforeEach
    void setUp() {
        provinceMapper = new ProvinceMapperImpl();
        userMapper = new UserMapperImpl();
        ReflectionTestUtils.setField(userMapper, "provinceMapper", provinceMapper);
        walletMapper = new WalletMapperImpl();
        transactionMapper = new TransactionMapperImpl();
        transferMapper = new TransferMapperImpl();
    }

    @Test
    void testWalletMapperAdminResponse() {
        UUID walletUid = UUID.randomUUID();
        UUID userUid = UUID.randomUUID();
        User user = User.builder()
                .uid(userUid)
                .fullName("John Doe")
                .phone("0987654321")
                .email("john@example.com")
                .build();
        Wallet wallet = Wallet.builder()
                .uid(walletUid)
                .walletNumber("W123456")
                .walletName("My Wallet")
                .balance(500000L)
                .user(user)
                .build();

        WalletAdminResponse response = walletMapper.mapToWalletAdminResponse(wallet);

        assertThat(response).isNotNull();
        assertThat(response.getUid()).isEqualTo(walletUid.toString());
        assertThat(response.getWalletNumber()).isEqualTo("W123456");
        assertThat(response.getUserUid()).isEqualTo(userUid.toString());
        assertThat(response.getUserFullName()).isEqualTo("John Doe");
        assertThat(response.getUserPhone()).isEqualTo("0987654321");
        assertThat(response.getUserEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testUserMapperProfileAndAdmin() {
        UUID userUid = UUID.randomUUID();
        Province province = Province.builder()
                .code("79")
                .name("TP. Ho Chi Minh")
                .build();
        User user = User.builder()
                .uid(userUid)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .province(province)
                .build();

        UserProfileResponse profileResponse = userMapper.mapToUserProfileResponse(user);
        assertThat(profileResponse).isNotNull();
        assertThat(profileResponse.getFullName()).isEqualTo("Jane Doe");
        assertThat(profileResponse.getProvinceResponse()).isNotNull();
        assertThat(profileResponse.getProvinceResponse().getName()).isEqualTo("TP. Ho Chi Minh");

        UserAdminResponse adminResponse = userMapper.mapToUserAdminResponse(user);
        assertThat(adminResponse).isNotNull();
        assertThat(adminResponse.getUid()).isEqualTo(userUid.toString());
        assertThat(adminResponse.getFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void testTransferMapper() {
        UUID txId = UUID.randomUUID();
        UUID fromUid = UUID.randomUUID();
        UUID toUid = UUID.randomUUID();

        Wallet fromWallet = Wallet.builder().uid(fromUid).build();
        Wallet toWallet = Wallet.builder().uid(toUid).build();

        Transaction tx = Transaction.builder()
                .id(txId)
                .referenceCode("REF123")
                .amount(100000L)
                .fromWallet(fromWallet)
                .toWallet(toWallet)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .build();

        TransferResponse response = transferMapper.mapToTransferResponse(tx);
        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo(txId.toString());
        assertThat(response.getFromWalletId()).isEqualTo(fromUid.toString());
        assertThat(response.getToWalletId()).isEqualTo(toUid.toString());
        assertThat(response.getReferenceCode()).isEqualTo("REF123");
        assertThat(response.getAmount()).isEqualTo(100000L);
    }

    @Test
    void testTransactionMapper() {
        UUID txId = UUID.randomUUID();
        User fromUser = User.builder().fullName("Sender").build();
        User toUser = User.builder().fullName("Receiver").build();

        Wallet fromWallet = Wallet.builder().walletNumber("W_FROM").user(fromUser).build();
        Wallet toWallet = Wallet.builder().walletNumber("W_TO").user(toUser).build();

        Transaction tx = Transaction.builder()
                .id(txId)
                .referenceCode("REF_TX")
                .amount(200000L)
                .fromWallet(fromWallet)
                .toWallet(toWallet)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description("Payment")
                .createdAt(Instant.now())
                .build();

        LedgerEntry entry = LedgerEntry.builder()
                .transaction(tx)
                .entryType(EntryType.DEBIT)
                .amount(200000L)
                .balanceAfter(800000L)
                .build();

        TransactionHistoryResponse historyResponse = transactionMapper.toHistoryResponse(entry);
        assertThat(historyResponse).isNotNull();
        assertThat(historyResponse.getTransactionId()).isEqualTo(txId.toString());
        assertThat(historyResponse.getReferenceCode()).isEqualTo("REF_TX");
        assertThat(historyResponse.getBalanceAfter()).isEqualTo(800000L);

        TransactionDetailResponse detailResponse = transactionMapper.toDetailResponse(entry);
        assertThat(detailResponse).isNotNull();
        assertThat(detailResponse.getTransactionId()).isEqualTo(txId.toString());
        assertThat(detailResponse.getFromWalletNumber()).isEqualTo("W_FROM");
        assertThat(detailResponse.getFromOwnerName()).isEqualTo("Sender");
        assertThat(detailResponse.getToWalletNumber()).isEqualTo("W_TO");
        assertThat(detailResponse.getToOwnerName()).isEqualTo("Receiver");
        assertThat(detailResponse.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(detailResponse.getBalanceAfter()).isEqualTo(800000L);
    }
}
