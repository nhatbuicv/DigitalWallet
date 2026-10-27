package com.nhatbui.digital_wallet.repository.specification;

import com.nhatbui.digital_wallet.dto.request.admin.WalletSearchRequest;
import com.nhatbui.digital_wallet.entity.User;
import com.nhatbui.digital_wallet.entity.Wallet;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class WalletSpecification {

    private WalletSpecification() {
    }

    public static Specification<Wallet> buildSearchSpec(WalletSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request == null) {
                return cb.conjunction();
            }

            // Join với User để filter theo thông tin người dùng
            Join<Wallet, User> userJoin = root.join("user", JoinType.LEFT);

            // Tìm theo số ví
            if (StringUtils.hasText(request.getWalletNumber())) {
                predicates.add(cb.like(root.get("walletNumber"),
                        "%" + request.getWalletNumber() + "%"));
            }

            // Tìm theo SĐT chủ ví
            if (StringUtils.hasText(request.getUserPhone())) {
                predicates.add(cb.like(cb.lower(userJoin.get("phone")),
                        "%" + request.getUserPhone().toLowerCase() + "%"));
            }

            // Tìm theo email
            if (StringUtils.hasText(request.getUserEmail())) {
                predicates.add(cb.like(cb.lower(userJoin.get("email")),
                        "%" + request.getUserEmail().toLowerCase() + "%"));
            }

            // Tìm theo họ tên chủ ví
            if (StringUtils.hasText(request.getUserName())) {
                predicates.add(cb.like(cb.lower(userJoin.get("fullName")),
                        "%" + request.getUserName().toLowerCase() + "%"));
            }

            // Lọc theo trạng thái ví
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // Lọc theo loại ví
            if (request.getWalletType() != null) {
                predicates.add(cb.equal(root.get("walletType"), request.getWalletType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}