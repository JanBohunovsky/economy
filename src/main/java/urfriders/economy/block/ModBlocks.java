package urfriders.economy.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import urfriders.economy.Economy;
import urfriders.economy.item.ModItemGroup;

public class ModBlocks {
    public static final Block TRADING_STATION = new TradingStationBlock();

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, new Identifier(Economy.MOD_ID, "trading_station"), TRADING_STATION);
    }

    public static void registerBlockItems() {
        Registry.register(Registry.ITEM, new Identifier(Economy.MOD_ID, "trading_station"), new BlockItem(TRADING_STATION, new FabricItemSettings().group(ModItemGroup.ALL)));
    }
}
