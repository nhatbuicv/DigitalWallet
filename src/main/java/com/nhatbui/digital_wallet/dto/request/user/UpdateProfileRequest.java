package com.nhatbui.digital_wallet.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    @NotBlank
    @Size(max = 100)
    private String fullName;

    @Past
    private LocalDate dateOfBirth;

    @Size(max = 500)
    private String address;

    private String provinceCode;
}