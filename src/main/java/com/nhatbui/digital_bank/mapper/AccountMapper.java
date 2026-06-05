package com.nhatbui.digital_bank.mapper;

import com.nhatbui.digital_bank.dto.response.BalanceResponse;
import com.nhatbui.digital_bank.dto.response.account.AccountResponse;
import com.nhatbui.digital_bank.dto.response.account.AccountSummaryResponse;
import com.nhatbui.digital_bank.entity.Account;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountResponse mapToAccountResponse(Account account);

    List<AccountSummaryResponse> mapToAccountSummaryResponse(List<Account> accounts);

    BalanceResponse mapToBalanceResponse(Account account);
}
