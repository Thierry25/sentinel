package com.sentinel.drills.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Trade {
    @Id
    private Integer tradeId;

    private String symbol;

    private String side;

    private Integer qty;

    private BigDecimal price;

    private LocalDateTime tradeTs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    protected Trade() {}

    public Integer getTradeId() {
        return tradeId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getTradeTs() {
        return tradeTs;
    }

    public Account getAccount() {
        return account;
    }
}
