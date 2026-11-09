package com.nhatbui.digital_wallet.controller;

import com.nhatbui.digital_wallet.dto.request.wallet.DepositRequest;
import com.nhatbui.digital_wallet.dto.request.wallet.TransferRequest;
import com.nhatbui.digital_wallet.dto.response.common.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.common.IdempotentResult;
import com.nhatbui.digital_wallet.dto.response.wallet.TransferResponse;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.service.transaction.DepositService;
import com.nhatbui.digital_wallet.service.transaction.TransactionService;
import com.nhatbui.digital_wallet.service.transaction.TransferService;
import com.nhatbui.digital_wallet.service.transaction.WithdrawService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransferService transferService;

    @Mock
    private DepositService depositService;

    @Mock
    private WithdrawService withdrawService;

    @Mock
    private TransactionService transactionService;

    private TransactionController controller;

    @BeforeEach
    void setUp() {
        controller = new TransactionController(
                transferService,
                depositService,
                withdrawService,
                transactionService);
    }

    @Test
    void missingIdempotencyKey_throwsIdempotencyKeyRequired() {
        DepositRequest request = new DepositRequest();
        request.setWalletNumber("123456789012");
        request.setAmount(10_000L);
        // headerKey is null, request.idempotencyKey is null

        assertThatThrownBy(() -> controller.deposit(null, request))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.IDEMPOTENCY_KEY_REQUIRED));
    }

    @Test
    void mismatchedHeaderAndBodyKey_throwsInvalidRequestBody() {
        DepositRequest request = new DepositRequest();
        request.setWalletNumber("123456789012");
        request.setAmount(10_000L);
        request.setIdempotencyKey("body-key-123");

        assertThatThrownBy(() -> controller.deposit("header-key-456", request))
                .isInstanceOfSatisfying(AppException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST_BODY));
    }

    @Test
    void headerKeyPreferred_andInjectedIntoRequest() {
        TransferRequest request = new TransferRequest();
        request.setFromWalletNumber("123456789012");
        request.setToWalletNumber("987654321098");
        request.setAmount(50_000L);

        ApiResponse<TransferResponse> apiResponse = ApiResponse.<TransferResponse>builder()
                .success(true)
                .build();
        when(transferService.transfer(any())).thenReturn(IdempotentResult.fresh(apiResponse));

        ResponseEntity<ApiResponse<TransferResponse>> response =
                controller.transfer("custom-header-key", request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().containsKey("Idempotent-Replay")).isFalse();
        assertThat(request.getIdempotencyKey()).isEqualTo("custom-header-key");
        verify(transferService).transfer(request);
    }

    @Test
    void replayedResponse_includesIdempotentReplayHeader() {
        TransferRequest request = new TransferRequest();
        request.setFromWalletNumber("123456789012");
        request.setToWalletNumber("987654321098");
        request.setAmount(50_000L);

        ApiResponse<TransferResponse> apiResponse = ApiResponse.<TransferResponse>builder()
                .success(true)
                .build();
        when(transferService.transfer(any())).thenReturn(IdempotentResult.replay(apiResponse));

        ResponseEntity<ApiResponse<TransferResponse>> response =
                controller.transfer("replayed-key", request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getFirst("Idempotent-Replay")).isEqualTo("true");
    }
}
