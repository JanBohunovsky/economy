package dev.bohush.economy.util;

import com.google.common.collect.Maps;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;

public class CriteriaRegistry {
    private static final Map<Identifier, Criterion<?>> VALUES = Maps.newHashMap();

    public static <T extends Criterion<?>> T register(T criterion) {
        if (VALUES.containsKey(criterion.getId())) {
            throw new IllegalArgumentException("Duplicate criterion id " + criterion.getId());
        } else {
            VALUES.put(criterion.getId(), criterion);
            return criterion;
        }
    }

    public static Map<Identifier, Criterion<?>> getCriteria() {
        return Collections.unmodifiableMap(VALUES);
    }
}
