package com.uco.ucopetapi.domain.purchases;

import java.math.BigDecimal;

public enum TaxCategory {
    STANDARD(new BigDecimal("0.19")),
    REDUCED(new BigDecimal("0.05")),
    EXEMPT(BigDecimal.ZERO);

    private final BigDecimal rate;

    TaxCategory(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getRate() {
        return rate;
    }
}
