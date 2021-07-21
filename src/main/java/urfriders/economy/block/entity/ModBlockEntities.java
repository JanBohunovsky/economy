package urfriders.economy.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;
import urfriders.economy.block.ModBlocks;
import urfriders.economy.block.ShopBlock;

public class ModBlockEntities {
    public static final BlockEntityType<ShopBlockEntity> SHOP = FabricBlockEntityTypeBuilder
        .create(ShopBlockEntity::new, ModBlocks.SHOP)
        .build();

    public static void registerBlockEntities() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, ShopBlock.ID, SHOP);
    }
}
