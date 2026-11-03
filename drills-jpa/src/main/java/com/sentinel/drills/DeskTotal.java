package com.sentinel.drills;

import java.math.BigDecimal;

public record DeskTotal(String deskId,
                        Long trades,
                        BigDecimal notional) {}
