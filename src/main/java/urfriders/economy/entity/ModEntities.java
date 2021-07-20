package urfriders.economy.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import urfriders.economy.Economy;

public class ModEntities {
    public static final EntityType<ShopVillagerEntity> SHOP_VILLAGER = FabricEntityTypeBuilder
        .create(SpawnGroup.MISC, ShopVillagerEntity::new)
        .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
        .trackRangeBlocks(10)
        .build();

    public static void registerEntities() {
        Registry.register(Registry.ENTITY_TYPE, new Identifier(Economy.MOD_ID, "shop_villager"), SHOP_VILLAGER);
        FabricDefaultAttributeRegistry.register(SHOP_VILLAGER, ShopVillagerEntity.createVillagerAttributes());
    }
}
