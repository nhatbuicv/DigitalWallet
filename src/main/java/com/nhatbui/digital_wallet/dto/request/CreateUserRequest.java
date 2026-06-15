package com.nhatbui.digital_wallet.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhatbui.digital_wallet.validator.PasswordMatch;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDate;

@PasswordMatch(message = "PASSWORD_NOT_MATCH")
@Getter
public class CreateUserRequest {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Invalid Vietnamese phone number")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    private String confirmPassword;

    @Pattern(regexp = "^[0-9]{2}$", message = "Invalid citizen ID")
    private String cityCode;

    @NotNull(message = "Date of birth is required")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;

    private String address;
}
