package com.nhatbui.digital_wallet.repository.specification;

import com.nhatbui.digital_wallet.dto.request.admin.UserSearchRequest;
import com.nhatbui.digital_wallet.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> buildSearchSpec(UserSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request == null) {
                return cb.conjunction();
            }

            // Tìm theo số điện thoại
            if (StringUtils.hasText(request.getPhone())) {
                predicates.add(cb.like(cb.lower(root.get("phone")),
                        "%" + request.getPhone().toLowerCase() + "%"));
            }

            // Tìm theo email
            if (StringUtils.hasText(request.getEmail())) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + request.getEmail().toLowerCase() + "%"));
            }

            // Tìm theo fullName
            if (StringUtils.hasText(request.getFullName())) {
                predicates.add(cb.like(cb.lower(root.get("fullName")),
                        "%" + request.getFullName().toLowerCase() + "%"));
            }

            // Lọc theo status
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // Lọc theo role
            if (request.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), request.getRole()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
