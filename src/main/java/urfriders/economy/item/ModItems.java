package urfriders.economy.item;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import urfriders.economy.Economy;

public class ModItems {
    public static final CoinItem COPPER_COIN = new CoinItem(0);
    public static final CoinItem IRON_COIN = new CoinItem(1);
    public static final CoinItem GOLD_COIN = new CoinItem(2);
    public static final CoinItem DIAMOND_COIN = new CoinItem(3);
    public static final CoinItem NETHERITE_COIN = new CoinItem(4);
    public static final Item PLATINUM_COIN = new BasicItem();

    public static final Item COPPER_COIN_PILE = new CoinPileItem(0);
    public static final Item IRON_COIN_PILE = new CoinPileItem(1);
    public static final Item GOLD_COIN_PILE = new CoinPileItem(2);
    public static final Item DIAMOND_COIN_PILE = new CoinPileItem(3);
    public static final Item NETHERITE_COIN_PILE = new CoinPileItem(4);
    public static final Item PLATINUM_COIN_PILE = new BasicItem();

    public static void registerItems() {
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "copper_coin"), COPPER_COIN);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "iron_coin"), IRON_COIN);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "gold_coin"), GOLD_COIN);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "diamond_coin"), DIAMOND_COIN);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "netherite_coin"), NETHERITE_COIN);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "platinum_coin"), PLATINUM_COIN);

        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "copper_coin_pile"), COPPER_COIN_PILE);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "iron_coin_pile"), IRON_COIN_PILE);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "gold_coin_pile"), GOLD_COIN_PILE);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "diamond_coin_pile"), DIAMOND_COIN_PILE);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "netherite_coin_pile"), NETHERITE_COIN_PILE);
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "platinum_coin_pile"), PLATINUM_COIN_PILE);
    }
}
