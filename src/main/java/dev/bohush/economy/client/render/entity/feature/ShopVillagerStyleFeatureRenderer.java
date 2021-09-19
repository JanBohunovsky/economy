package dev.bohush.economy.client.render.entity.feature;

import dev.bohush.economy.client.render.entity.model.ShopVillagerEntityModel;
import dev.bohush.economy.entity.ShopVillagerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShopVillagerStyleFeatureRenderer extends FeatureRenderer<ShopVillagerEntity, ShopVillagerEntityModel> {
    private static final Logger LOGGER = LogManager.getLogger();

    public ShopVillagerStyleFeatureRenderer(FeatureRendererContext<ShopVillagerEntity, ShopVillagerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ShopVillagerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (entity.isInvisible()) {
            return;
        }

        var shopBlockEntity = entity.getShopBlockEntity();
        if (shopBlockEntity == null) {
            return;
        }

        var villagerStyle = shopBlockEntity.getVillagerStyle();
        if (villagerStyle == null) {
            return;
        }

        var entityModel = this.getContextModel();
        var biomeClothes = villagerStyle.getBiomeClothes();
        var professionClothes = villagerStyle.getProfessionClothes();

        if (biomeClothes != null) {
            // Show "hat" only when profession clothes also have "hat" or there are no profession clothes set
            entityModel.setHatVisible(biomeClothes.showHat() && (professionClothes == null || !professionClothes.showHat()));
            renderModel(entityModel, biomeClothes.getTexture(), matrices, vertexConsumers, light, entity, 1, 1, 1);
        }

        if (professionClothes != null) {
            entityModel.setHatVisible(professionClothes.showHat());
            renderModel(entityModel, professionClothes.getTexture(), matrices, vertexConsumers, light, entity, 1, 1, 1);
        }

        entityModel.setHatVisible(true);
        entityModel.setBodyVisible(false);

        var hat = villagerStyle.getHat();
        if (hat != null) {
            renderModel(entityModel, hat.getTexture(), matrices, vertexConsumers, light, entity, 1, 1, 1);
        }

        var accessory = villagerStyle.getAccessory();
        if (accessory != null) {
            renderModel(entityModel, accessory.getTexture(), matrices, vertexConsumers, light, entity, 1, 1, 1);
        }

        entityModel.setBodyVisible(true);
    }
}
