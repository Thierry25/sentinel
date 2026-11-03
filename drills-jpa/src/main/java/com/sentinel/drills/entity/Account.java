package com.sentinel.drills.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Account {
    @Id
    private String accountId;

    private String owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESK_ID")
    private Desk desk;

    @OneToMany(mappedBy = "account")
    private List<Trade> trades;

    @OneToMany(mappedBy = "account")
    private List<TradeOrder> orders;

    protected Account() {}

    public String getAccountId() {
        return accountId;
    }

    public String getOwner() {
        return owner;
    }

    public Desk getDesk() {
        return desk;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public List<TradeOrder> getOrders() {
        return orders;
    }
}
