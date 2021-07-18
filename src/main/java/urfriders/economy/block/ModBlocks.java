package urfriders.economy.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.registry.Registry;
import urfriders.economy.item.ModItemGroup;

public class ModBlocks {
    public static final Block TRADING_STATION = new TradingStationBlock();

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, TradingStationBlock.ID, TRADING_STATION);
    }

    public static void registerBlockItems() {
        Registry.register(Registry.ITEM, TradingStationBlock.ID, new BlockItem(TRADING_STATION, new FabricItemSettings().group(ModItemGroup.ALL)));
    }
}
