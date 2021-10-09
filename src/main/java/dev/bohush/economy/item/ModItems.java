package dev.bohush.economy.item;

import dev.bohush.economy.Economy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class ModItems {
    public static final Item MOD_ICON = new Item(new FabricItemSettings());

    public static final CoinItem COPPER_COIN = new CoinItem(0);
    public static final CoinItem IRON_COIN = new CoinItem(1);
    public static final CoinItem GOLD_COIN = new CoinItem(2);
    public static final CoinItem NETHERITE_COIN = new CoinItem(3);

    public static final Item COIN_PILE = new CoinPileItem();

    public static final Item COPPER_NUGGET = new Item(new FabricItemSettings().group(ItemGroup.MISC));
    public static final Item NETHERITE_NUGGET = new Item(new FabricItemSettings().group(ItemGroup.MISC));

    public static void registerItems() {
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "gold_coin_pile"), MOD_ICON);

        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "copper_coin"), COPPER_COIN);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "iron_coin"), IRON_COIN);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "gold_coin"), GOLD_COIN);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "netherite_coin"), NETHERITE_COIN);

        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "coin_pile"), COIN_PILE);

        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "copper_nugget"), COPPER_NUGGET);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "netherite_nugget"), NETHERITE_NUGGET);
    }

    @Environment(EnvType.CLIENT)
    public static void registerItemPredicates() {
        FabricModelPredicateProviderRegistry.register(
            COIN_PILE,
            new Identifier("copper_coin_count"),
            new CoinCountModelPredicateProvider(CoinPileItem::getCopperCoins)
        );

        FabricModelPredicateProviderRegistry.register(
            COIN_PILE,
            new Identifier("iron_coin_count"),
            new CoinCountModelPredicateProvider(CoinPileItem::getIronCoins)
        );

        FabricModelPredicateProviderRegistry.register(
            COIN_PILE,
            new Identifier("gold_coin_count"),
            new CoinCountModelPredicateProvider(CoinPileItem::getGoldCoins)
        );

        FabricModelPredicateProviderRegistry.register(
            COIN_PILE,
            new Identifier("netherite_coin_count"),
            new CoinCountModelPredicateProvider(CoinPileItem::getNetheriteCoins)
        );
    }

    @Environment(EnvType.CLIENT)
    private static class CoinCountModelPredicateProvider implements UnclampedModelPredicateProvider {

        private final CoinCountProvider provider;

        public CoinCountModelPredicateProvider(CoinCountProvider provider) {
            this.provider = provider;
        }

        @Override
        public float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity livingEntity, int seed) {
            return this.unclampedCall(stack, world, livingEntity, seed);
        }

        @Override
        public float unclampedCall(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
            var amount = this.provider.getAmount(stack);
            if (amount > 100) {
                return 100;
            }

            return amount;
        }

        public interface CoinCountProvider {
            long getAmount(ItemStack stack);
        }
    }
}
