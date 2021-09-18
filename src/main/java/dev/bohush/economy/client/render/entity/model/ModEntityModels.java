package dev.bohush.economy.client.render.entity.model;

import dev.bohush.economy.client.render.entity.ShopVillagerEntityRenderer;
import dev.bohush.economy.entity.ModEntities;
import dev.bohush.economy.entity.ShopVillagerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;

@Environment(EnvType.CLIENT)
public class ModEntityModels {
    public static final EntityModelLayer SHOP_VILLAGER_LAYER = new EntityModelLayer(ShopVillagerEntity.ID, "main");

    public static void registerModelsAndLayers() {
        EntityRendererRegistry.INSTANCE.register(ModEntities.SHOP_VILLAGER, ShopVillagerEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(SHOP_VILLAGER_LAYER, ShopVillagerEntityModel::getTexturedModelData);
    }
}
