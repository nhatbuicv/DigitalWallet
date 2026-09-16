package com.nhatbui.digital_wallet.repository.specification;

import com.nhatbui.digital_wallet.dto.request.admin.AuditLogSearchRequest;
import com.nhatbui.digital_wallet.entity.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {

    private AuditLogSpecification() {
    }

    public static Specification<AuditLog> buildSearchSpec(AuditLogSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request == null) {
                return cb.conjunction();
            }

            if (request.getActorUid() != null) {
                predicates.add(cb.equal(root.get("actorUid"), request.getActorUid()));
            }
            if (request.getAction() != null) {
                predicates.add(cb.equal(root.get("action"), request.getAction()));
            }
            if (request.getTargetType() != null) {
                predicates.add(cb.equal(root.get("targetType"), request.getTargetType()));
            }
            if (request.getTargetUid() != null) {
                predicates.add(cb.equal(root.get("targetUid"), request.getTargetUid()));
            }
            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getToDate()));
            }

            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
