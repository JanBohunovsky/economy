package dev.bohush.economy.advancement.criterion;

import dev.bohush.economy.util.CriteriaRegistry;

public class ModCriteria {
    public static final CoinsChangedCriterion COINS_CHANGED = new CoinsChangedCriterion();

    public static void registerCriteria() {
        CriteriaRegistry.register(COINS_CHANGED);
    }
}
