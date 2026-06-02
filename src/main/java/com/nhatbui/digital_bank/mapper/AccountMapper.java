package com.nhatbui.digital_bank.mapper;

import com.nhatbui.digital_bank.dto.response.account.AccountResponse;
import com.nhatbui.digital_bank.entity.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountResponse mapToAccountResponse(Account account);
}
