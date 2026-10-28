package com.nhatbui.digital_wallet.repository.specification;

import com.nhatbui.digital_wallet.dto.request.admin.TransactionSearchRequest;
import com.nhatbui.digital_wallet.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    private TransactionSpecification() {
    }

    public static Specification<Transaction> buildSearchSpec(TransactionSearchRequest request) {
        return (root, query, cb) -> {
            if (request == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            // Tìm theo mã giao dịch
            if (StringUtils.hasText(request.getReference())) {
                predicates.add(cb.like(cb.lower(root.get("referenceCode")),
                        "%" + request.getReference().trim().toLowerCase() + "%"));
            }

            // Lọc theo status
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // Lọc theo type
            if (request.getType() != null) {
                predicates.add(cb.equal(root.get("type"), request.getType()));
            }

            // Lọc theo khoảng thời gian tạo giao dịch
            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
