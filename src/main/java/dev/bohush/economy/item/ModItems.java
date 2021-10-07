package dev.bohush.economy.item;

import dev.bohush.economy.Economy;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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
}
