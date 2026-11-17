package com.nhatbui.wallet_ledger.repository;

import com.nhatbui.wallet_ledger.entity.Transaction;
import com.nhatbui.wallet_ledger.entity.User;
import com.nhatbui.wallet_ledger.entity.Wallet;
import com.nhatbui.wallet_ledger.repository.projection.TransactionAdminProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionRepositoryCustomImpl implements TransactionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<TransactionAdminProjection> findProjectedBySpec(Specification<Transaction> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<TransactionAdminProjection> cq = cb.createQuery(TransactionAdminProjection.class);
        Root<Transaction> root = cq.from(Transaction.class);
        Join<Transaction, Wallet> fromWallet = root.join("fromWallet", JoinType.LEFT);
        Join<Wallet, User> fromUser = fromWallet.join("user", JoinType.LEFT);
        Join<Transaction, Wallet> toWallet = root.join("toWallet", JoinType.LEFT);
        Join<Wallet, User> toUser = toWallet.join("user", JoinType.LEFT);

        cq.select(cb.construct(
                TransactionAdminProjection.class,
                root.get("id"),
                root.get("referenceCode"),
                root.get("type"),
                root.get("status"),
                root.get("currency"),
                root.get("amount"),
                root.get("description"),
                fromWallet.get("uid"),
                fromWallet.get("walletNumber"),
                fromUser.get("fullName"),
                toWallet.get("uid"),
                toWallet.get("walletNumber"),
                toUser.get("fullName"),
                root.get("createdAt"),
                root.get("completedAt")
        ));

        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, cq, cb);
            if (predicate != null) {
                cq.where(predicate);
            }
        }

        if (pageable.getSort().isSorted()) {
            cq.orderBy(QueryUtils.toOrders(pageable.getSort(), root, cb));
        }

        TypedQuery<TransactionAdminProjection> query = entityManager.createQuery(cq);
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        List<TransactionAdminProjection> content = query.getResultList();
        return PageableExecutionUtils.getPage(content, pageable, () -> countBySpec(spec));
    }

    private long countBySpec(Specification<Transaction> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Transaction> countRoot = countQuery.from(Transaction.class);
        countQuery.select(cb.count(countRoot));

        if (spec != null) {
            Predicate predicate = spec.toPredicate(countRoot, countQuery, cb);
            if (predicate != null) {
                countQuery.where(predicate);
            }
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
