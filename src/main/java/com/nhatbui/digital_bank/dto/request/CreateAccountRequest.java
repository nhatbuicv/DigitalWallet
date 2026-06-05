package com.nhatbui.digital_bank.dto.request;

import com.nhatbui.digital_bank.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAccountRequest {
    @NotNull
    private AccountType accountType;
}
