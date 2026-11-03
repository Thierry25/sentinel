package com.sentinel.drills;

import com.sentinel.drills.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DrillQueries {
    private final EntityManager em;

    public DrillQueries(EntityManager em) {
        this.em = em;
    }

    // the trades of one desk in a time range, in time order
    public List<Trade> tradesOfDeskBetween(String deskId, LocalDateTime from, LocalDateTime to) {
        var jpql = """
                    SELECT t FROM Trade t
                    WHERE t.account.desk.deskId =:deskId
                    AND tradeTs BETWEEN :from AND :to
                """;
        return em.createQuery(jpql, Trade.class)
                .setParameter("deskId", deskId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    // per desk: number of trades and total notional (qty × price) in a time range, biggest first
    public List<DeskTotal> deskTotalsBetween(LocalDateTime from, LocalDateTime now) {
        var jqpl = """
                SELECT new com.sentinel.drills.DeskTotal(t.account.desk.deskId, COUNT(t), SUM(t.qty * t.price))
                FROM Trade t
                WHERE tradeTs BETWEEN :from AND :to
                GROUP BY t.account.desk.deskId
                ORDER BY SUM(t.qty * t.price) DESC
                """;

        return em.createQuery(jqpl, DeskTotal.class)
                .setParameter("from", from)
                .setParameter("to", now)
                .getResultList();
    }

    public List<Account> accountsWithTrades() {
        var jpql = """
                    SELECT DISTINCT a FROM Account a
                    LEFT JOIN FETCH a.trades
                """;
        return em.createQuery(jpql, Account.class)
                .getResultList();
    }

    // orders matching any combination of 4 optional filters, ordered by id
    public List<TradeOrder> searchOrders(OrderSearch search) {
        var cb = em.getCriteriaBuilder();
        CriteriaQuery<TradeOrder> cq = cb.createQuery(TradeOrder.class);

        Root<TradeOrder> root = cq.from(TradeOrder.class);
        List<Predicate> predicates = new ArrayList<>();

        Join<TradeOrder, Account> orderAccountJoin = root.join("account");
        var accountId = search.accountId();
        if (accountId != null && !accountId.isEmpty()) {
            predicates.add(cb.equal(orderAccountJoin.get("accountId"), accountId));
        }

        var status = search.status();
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        if (search.marketOnly()) {
            predicates.add(cb.isNull(root.get("limitPrice")));
        }

        var minQty = search.minQuantity();
        if (minQty != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("qty"), minQty));
        }

        cq.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.asc(root.get("orderId")));

        return em.createQuery(cq).getResultList();
    }

    // accounts with at least N cancelled orders, with the count, biggest first then by id
    public List<AccountCount> accountsWithAtLeastCancels(long minimum) {
        /*
        SELECT account_id, COUNT(*),
        FROM ORDERS
        WHERE status = 'CANCELLED'
        GROUP BY account_id
        HAVING COUNT(*) >= minimum
        ORDER BY COUNT(*)
         */

        var cb = em.getCriteriaBuilder();
        CriteriaQuery<AccountCount> cq = cb.createQuery(AccountCount.class);

        Root<TradeOrder> root = cq.from(TradeOrder.class);
        Join<TradeOrder, Account> accountJoin = root.join("account");

        cq.select(cb.construct(AccountCount.class,
                accountJoin.get("accountId"),
                cb.count(root)
        )).orderBy(cb.desc(cb.count(root)));

        cq.where(cb.equal(root.get("status"), OrderStatus.CANCELLED.name()));

        cq.groupBy(accountJoin.get("accountId"));
        cq.having(cb.ge(cb.count(root), minimum));

        return em.createQuery(cq).getResultList();
    }
}

