package com.nhatbui.digital_wallet.service.impl;

import com.nhatbui.digital_wallet.config.SecurityUtils;
import com.nhatbui.digital_wallet.dto.request.CreateWalletRequest;
import com.nhatbui.digital_wallet.dto.response.ApiResponse;
import com.nhatbui.digital_wallet.dto.response.BalanceResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletResponse;
import com.nhatbui.digital_wallet.dto.response.wallet.WalletSummaryResponse;
import com.nhatbui.digital_wallet.entity.Wallet;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.enums.WalletStatus;
import com.nhatbui.digital_wallet.enums.WalletType;
import com.nhatbui.digital_wallet.enums.CurrencyCode;
import com.nhatbui.digital_wallet.exception.AppException;
import com.nhatbui.digital_wallet.exception.ErrorCode;
import com.nhatbui.digital_wallet.generator.WalletNumberGenerator;
import com.nhatbui.digital_wallet.mapper.WalletMapper;
import com.nhatbui.digital_wallet.repository.WalletRepository;
import com.nhatbui.digital_wallet.repository.UserRepository;
import com.nhatbui.digital_wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.nhatbui.digital_wallet.enums.WalletStatus.*;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletNumberGenerator walletNumberGenerator;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final SecurityUtils securityUtils;

    @Override
    public Wallet createDefaultWallet(User user) {
        String walletNumber = walletNumberGenerator.generate(null, null);

        return Wallet.builder()
                .user(user)
                .walletNumber(walletNumber)
                .walletName(user.getFullName())
                .walletType(WalletType.CHECKING)
                .currency(CurrencyCode.VND)
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .blockedBalance(BigDecimal.ZERO)
                .status(PENDING)
                .branchCode("099")
                .branchName("Online Branch")
                .dailyLimit(new BigDecimal("50000000"))
                .monthlyLimit(new BigDecimal("500000000"))
                .openedAt(Instant.now())
                .build();
    }


    @Override
    public ApiResponse<List<WalletSummaryResponse>> getWalletsByUserUid(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        List<Wallet> wallets = walletRepository.findAllByUserUid(userId);
        List<WalletSummaryResponse> data = walletMapper.mapToWalletSummaryResponse(wallets);

        return ApiResponse.<List<WalletSummaryResponse>>builder()
                .message("Thành công")
                .success(true)
                .data(data)
                .build();
    }

    @Override
    public ApiResponse<WalletResponse> getWalletByWalletNumber(String walletNumber, UUID userUid) {
        Wallet wallet = walletRepository.findByWalletNumberAndUserUid(walletNumber, userUid)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        WalletResponse data = walletMapper.mapToWalletResponse(wallet);

        return ApiResponse.<WalletResponse>builder()
                .message("Thành công")
                .success(true)
                .data(data)
                .build();
    }

    @Override
    public ApiResponse<WalletResponse> createWallet(CreateWalletRequest request) {
        User user = securityUtils.getCurrentUser();

        String walletNumber = walletNumberGenerator.generate(null, request.getWalletType());

        Wallet wallet = Wallet.builder()
                .user(user)
                .walletNumber(walletNumber)
                .walletName(user.getFullName())
                .walletType(request.getWalletType())
                .currency(CurrencyCode.VND)
                .status(ACTIVE)
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .blockedBalance(BigDecimal.ZERO)
                .branchCode("099")
                .branchName("Online Branch")
                .dailyLimit(new BigDecimal("50000000"))
                .monthlyLimit(new BigDecimal("500000000"))
                .openedAt(Instant.now())
                .openedAt(Instant.now())
                .build();

        walletRepository.save(wallet);

        WalletResponse data = walletMapper.mapToWalletResponse(wallet);

        return ApiResponse.<WalletResponse>builder()
                .message("Tạo tài khoản thành công")
                .success(true)
                .data(data)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> changeWalletStatus(UUID walletId, WalletStatus newStatus) {
        User user = securityUtils.getCurrentUser();

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (!wallet.getUser().getUid().equals(user.getUid())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        WalletStatus currentStatus = wallet.getStatus();

        if (currentStatus == WalletStatus.CLOSED) {
            throw new AppException(ErrorCode.WALLET_CLOSED);
        }

        if (currentStatus == WalletStatus.INACTIVE || currentStatus == WalletStatus.PENDING || currentStatus == WalletStatus.SUSPENDED) {
            throw new AppException(ErrorCode.WALLET_STATUS_NOT_CHANGEABLE);
        }

        if (newStatus == WalletStatus.ACTIVE) {
            if (currentStatus != WalletStatus.FROZEN) {
                throw new AppException(ErrorCode.WALLET_ALREADY_CLOSED);
            }
            wallet.setStatus(WalletStatus.ACTIVE);

        } else if (newStatus == WalletStatus.FROZEN) {
            if (currentStatus != WalletStatus.ACTIVE) {
                throw new AppException(ErrorCode.WALLET_STATUS_NOT_CHANGEABLE);
            }

            long activeCount = walletRepository.countByUserUidAndStatus(user.getUid(), WalletStatus.ACTIVE);
            if (activeCount <= 1) {
                throw new AppException(ErrorCode.CANNOT_FREEZE_LAST_ACTIVE_WALLET);
            }
            wallet.setStatus(WalletStatus.FROZEN);

        } else {
            throw new AppException(ErrorCode.INVALID_WALLET_STATUS);
        }

        walletRepository.save(wallet);

        return ApiResponse.<Void>builder()
                .message(newStatus == WalletStatus.ACTIVE ? "Kích hoạt tài khoản thành công" : "Đóng băng tài khoản thành công")
                .success(true)
                .build();
    }

    @Override
    public ApiResponse<BalanceResponse> getBalance(String walletNumber) {
        User user = securityUtils.getCurrentUser();

        Wallet wallet = walletRepository.findByWalletNumber(walletNumber)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (!wallet.getUser().getUid().equals(user.getUid())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        switch (wallet.getStatus()) {
            case CLOSED -> throw new AppException(ErrorCode.WALLET_CLOSED);
            case SUSPENDED, INACTIVE, PENDING ,FROZEN -> throw new AppException(ErrorCode.INVALID_ACTION);
            case ACTIVE -> {}
            default -> throw new AppException(ErrorCode.WALLET_STATUS);
        }

        BalanceResponse data = walletMapper.mapToBalanceResponse(wallet);

        return ApiResponse.<BalanceResponse>builder()
                .message("Lấy số dư thành công")
                .success(true)
                .data(data)
                .build();
    }
}
