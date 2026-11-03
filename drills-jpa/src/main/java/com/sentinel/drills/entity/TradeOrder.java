package com.sentinel.drills.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ORDERS")
public class TradeOrder {
    @Id
    private Integer orderId;

    private String symbol;

    private String side;

    private Integer qty;

    private BigDecimal limitPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime placedTs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    protected TradeOrder() {}

    public Integer getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSide() {
        return side;
    }

    public Integer getQty() {
        return qty;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getPlacedTs() {
        return placedTs;
    }

    public Account getAccount() {
        return account;
    }
}
