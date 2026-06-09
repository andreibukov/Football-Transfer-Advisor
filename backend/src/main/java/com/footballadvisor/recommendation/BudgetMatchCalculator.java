package com.footballadvisor.recommendation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BudgetMatchCalculator {

    public double calculate(BigDecimal maxBudget, BigDecimal marketValue) {
        if (maxBudget == null || marketValue == null) {
            return 0;
        }

        if (maxBudget.signum() <= 0) {
            return 0;
        }

        if (marketValue.compareTo(maxBudget) <= 0) {
            return 100;
        }

        double overBudgetRatio = marketValue
                .subtract(maxBudget)
                .divide(maxBudget, 4, java.math.RoundingMode.HALF_UP)
                .doubleValue();

        return Math.max(0, 100 - (overBudgetRatio * 100));
    }
}
