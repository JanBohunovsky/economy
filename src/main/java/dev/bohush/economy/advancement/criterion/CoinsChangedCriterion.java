package dev.bohush.economy.advancement.criterion;

import com.google.gson.JsonObject;
import dev.bohush.economy.Economy;
import dev.bohush.economy.item.CoinPileItem;
import dev.bohush.economy.predicate.LongRange;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CoinsChangedCriterion extends AbstractCriterion<CoinsChangedCriterion.Conditions> {
    private static final Identifier ID = new Identifier(Economy.MOD_ID, "coins_changed");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject json, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        var value = LongRange.fromJson(json.get("value"));
        var copperCount = NumberRange.IntRange.fromJson(json.get("copper_count"));
        var ironCount = NumberRange.IntRange.fromJson(json.get("iron_count"));
        var goldCount = NumberRange.IntRange.fromJson(json.get("gold_count"));
        var netheriteCount = NumberRange.IntRange.fromJson(json.get("netherite_count"));

        return new Conditions(playerPredicate, value, copperCount, ironCount, goldCount, netheriteCount);
    }

    public void trigger(ServerPlayerEntity player, ItemStack stack) {
        var value = CoinPileItem.getValue(stack);
        this.trigger(player, conditions -> conditions.matches(value));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final LongRange value;
        private final NumberRange.IntRange copperCount;
        private final NumberRange.IntRange ironCount;
        private final NumberRange.IntRange goldCount;
        private final NumberRange.IntRange netheriteCount;

        public Conditions(
            EntityPredicate.Extended playerPredicate,
            LongRange value,
            NumberRange.IntRange copperCount,
            NumberRange.IntRange ironCount,
            NumberRange.IntRange goldCount,
            NumberRange.IntRange netheriteCount
        ) {
            super(ID, playerPredicate);
            this.value = value;
            this.copperCount = copperCount;
            this.ironCount = ironCount;
            this.goldCount = goldCount;
            this.netheriteCount = netheriteCount;
        }

        public boolean matches(long value) {
            if (!this.value.test(value)) {
                return false;
            }

            var copperCount = (int)CoinPileItem.getCopperCoins(value);
            if (!this.copperCount.test(copperCount)) {
                return false;
            }

            var ironCount = (int)CoinPileItem.getIronCoins(value);
            if (!this.ironCount.test(ironCount)) {
                return false;
            }

            var goldCount = (int)CoinPileItem.getGoldCoins(value);
            if (!this.goldCount.test(goldCount)) {
                return false;
            }

            var netheriteCount = (int)CoinPileItem.getNetheriteCoins(value);
            return this.netheriteCount.test(netheriteCount);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            var json = super.toJson(predicateSerializer);

            if (!this.value.isDummy()) {
                json.add("value", this.value.toJson());
            }

            if (!this.copperCount.isDummy()) {
                json.add("copper_count", this.copperCount.toJson());
            }

            if (!this.ironCount.isDummy()) {
                json.add("iron_count", this.ironCount.toJson());
            }

            if (!this.goldCount.isDummy()) {
                json.add("gold_count", this.goldCount.toJson());
            }

            if (!this.netheriteCount.isDummy()) {
                json.add("netherite_count", this.netheriteCount.toJson());
            }

            return json;
        }
    }
}
