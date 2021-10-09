package dev.bohush.economy.block;

import dev.bohush.economy.item.ModItemGroup;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.registry.Registry;

public class ModBlocks {
    public static final Block SHOP = new ShopBlock();

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, ShopBlock.ID, SHOP);
    }

    public static void registerBlockItems() {
        Registry.register(Registry.ITEM, ShopBlock.ID, new BlockItem(SHOP, new FabricItemSettings().group(ModItemGroup.ECONOMY)));
    }
}
