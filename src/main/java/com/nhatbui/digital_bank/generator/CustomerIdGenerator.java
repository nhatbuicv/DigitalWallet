package com.nhatbui.digital_bank.generator;

import com.nhatbui.digital_bank.entity.User;
import com.nhatbui.digital_bank.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerIdGenerator {

    UserRepository userRepository;

    public String generateCustomerId() {
        Optional<User> lastUser = userRepository.findTopByOrderByUidDesc();

        // Nếu chưa có account nào
        if (lastUser.isEmpty()) {
            return "CUS00000001";
        }

        String lastUserId = lastUser.get().getCustomerId();

        // Lấy phần số
        String numberPart = lastUserId.substring(3);

        int number = Integer.parseInt(numberPart);

        int nextNumber = number + 1;

        return String.format("CUS%08d", nextNumber);
    }
}
