package dev.bohush.economy.client.render.entity;

import dev.bohush.economy.client.render.entity.feature.ShopVillagerStyleFeatureRenderer;
import dev.bohush.economy.client.render.entity.model.ModEntityModels;
import dev.bohush.economy.client.render.entity.model.ShopVillagerEntityModel;
import dev.bohush.economy.entity.ShopVillagerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ShopVillagerEntityRenderer extends MobEntityRenderer<ShopVillagerEntity, ShopVillagerEntityModel> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/villager/villager.png");

    public ShopVillagerEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ShopVillagerEntityModel(context.getPart(ModEntityModels.SHOP_VILLAGER_LAYER)), 0.5F);
        this.addFeature(new ShopVillagerStyleFeatureRenderer(this));
        this.addFeature(new VillagerHeldItemFeatureRenderer<>(this));
    }

    @Override
    public Identifier getTexture(ShopVillagerEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(ShopVillagerEntity entity, MatrixStack matrices, float amount) {
        float g = 0.9375F;
        matrices.scale(g, g, g);
    }
}
