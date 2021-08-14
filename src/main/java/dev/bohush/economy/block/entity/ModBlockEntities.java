package dev.bohush.economy.block.entity;

import dev.bohush.economy.block.ModBlocks;
import dev.bohush.economy.block.ShopBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class ModBlockEntities {
    public static final BlockEntityType<ShopBlockEntity> SHOP = FabricBlockEntityTypeBuilder
        .create(ShopBlockEntity::new, ModBlocks.SHOP)
        .build();

    public static void registerBlockEntities() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, ShopBlock.ID, SHOP);
    }
}
