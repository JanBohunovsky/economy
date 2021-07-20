package urfriders.economy.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.registry.Registry;
import urfriders.economy.item.ModItemGroup;

public class ModBlocks {
    public static final Block PLAYER_SHOP = new PlayerShopBlock();

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, PlayerShopBlock.ID, PLAYER_SHOP);
    }

    public static void registerBlockItems() {
        Registry.register(Registry.ITEM, PlayerShopBlock.ID, new BlockItem(PLAYER_SHOP, new FabricItemSettings().group(ModItemGroup.ALL)));
    }
}
