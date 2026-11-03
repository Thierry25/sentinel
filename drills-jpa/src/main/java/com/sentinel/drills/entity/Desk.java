package com.sentinel.drills.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Desk {
    @Id
    private String deskId;

    private String name;

    protected Desk() {}

    public String getDeskId() {
        return deskId;
    }

    public String getName() {
        return name;
    }
}
