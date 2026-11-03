package com.sentinel.drills;

import com.sentinel.drills.entity.*;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest                                                                       // only the JPA layer, each test rolled back
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)       // the REAL Db2, not an in-memory database
@Import(DrillQueries.class)                                                        // @DataJpaTest doesn't scan plain @Repository classes
class DrillQueriesTest {

    private static final LocalDateTime OCT_27 = LocalDateTime.of(2026, 10, 27, 0, 0);
    private static final LocalDateTime OCT_28 = OCT_27.plusDays(1);

    @Autowired DrillQueries queries;
    @Autowired EntityManager em;

    @Test
    void j1_tradesOfTheEquitiesDeskOnOctober27InTimeOrder() {
        assertThat(queries.tradesOfDeskBetween("EQ", OCT_27, OCT_28))
                .extracting(Trade::getTradeId)
                .containsExactly(1, 2, 3, 9);
    }

    @Test
    void j2_totalsPerDeskOnOctober27() {
        var totals = queries.deskTotalsBetween(OCT_27, OCT_28);

        assertThat(totals).extracting(DeskTotal::deskId).containsExactly("EQ", "FI", "FX");
        assertThat(totals).extracting(DeskTotal::trades).containsExactly(4L, 2L, 1L);
        assertThat(totals.get(0).notional()).isEqualByComparingTo("210600");
        assertThat(totals.get(1).notional()).isEqualByComparingTo("138310");
        assertThat(totals.get(2).notional()).isEqualByComparingTo("54100");
    }

    @Test
    void j3_lazyLoadingCausesNPlusOne_andJoinFetchFixesIt() {
        var statistics = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();

        // the problem: 1 query for the accounts + 1 query PER ACCOUNT when its trades are touched
        em.clear();
        statistics.clear();
        var accounts = em.createQuery("select a from Account a", Account.class).getResultList();
        accounts.forEach(account -> account.getTrades().size());
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(6);      // 1 + 5 accounts = N+1

        // the fix: one query that brings the trades along
        em.clear();
        statistics.clear();
        var fetched = queries.accountsWithTrades();
        fetched.forEach(account -> account.getTrades().size());
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        assertThat(fetched).hasSize(5);
    }

    @Test
    void c1_searchWithAnyCombinationOfFilters() {
        assertThat(ids(new OrderSearch("A101", OrderStatus.CANCELLED, false, null))).containsExactly(7, 8, 9);
        assertThat(ids(new OrderSearch(null, null, true, null))).containsExactly(3, 4, 9, 11, 14);
        assertThat(ids(new OrderSearch(null, null, false, 1_000))).containsExactly(10, 12, 13, 14);
        assertThat(ids(new OrderSearch("A100", null, true, null))).containsExactly(3, 4);
        assertThat(ids(new OrderSearch(null, null, false, null))).hasSize(14);              // no filter at all
    }

    @Test
    void c2_accountsWithAtLeastNCancelledOrders() {
        assertThat(queries.accountsWithAtLeastCancels(2)).containsExactly(
                new AccountCount("A101", 3L), new AccountCount("A200", 3L), new AccountCount("A100", 2L));
        assertThat(queries.accountsWithAtLeastCancels(3))
                .extracting(AccountCount::accountId).containsExactly("A101", "A200");
    }

    private java.util.List<Integer> ids(OrderSearch search) {
        return queries.searchOrders(search).stream().map(TradeOrder::getOrderId).toList();
    }
}
