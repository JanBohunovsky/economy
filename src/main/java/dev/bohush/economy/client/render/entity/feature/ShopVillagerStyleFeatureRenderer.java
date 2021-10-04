package dev.bohush.economy.client.render.entity.feature;

import dev.bohush.economy.client.render.entity.model.ShopVillagerEntityModel;
import dev.bohush.economy.entity.ShopVillagerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
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

        var villagerStyle = entity.getStyle();
        if (villagerStyle == null) {
            return;
        }

        var model = this.getContextModel();
        var overlay = LivingEntityRenderer.getOverlay(entity, 0);
        var biomeClothes = villagerStyle.getBiomeClothes();
        var professionClothes = villagerStyle.getProfessionClothes();

        if (biomeClothes != null) {
            // Show "hat" only when profession clothes also have "hat" or there are no profession clothes set
            model.setHatVisible(biomeClothes.showHat() && (professionClothes == null || !professionClothes.showHat()));
            model.renderWithTexture(biomeClothes.getTexture(), matrices, vertexConsumers, light, overlay, 1, 1, 1, 1);
        }

        if (professionClothes != null) {
            model.setHatVisible(professionClothes.showHat());
            model.renderWithTexture(professionClothes.getTexture(), matrices, vertexConsumers, light, overlay, 1, 1, 1, 1);
        }

        model.setHatVisible(true);
        model.setBodyVisible(false);

        var hat = villagerStyle.getHat();
        if (hat != null) {
            model.renderWithTexture(hat.getTexture(), matrices, vertexConsumers, light, overlay, 1, 1, 1, 1);
        }

        var accessory = villagerStyle.getAccessory();
        if (accessory != null) {
            model.renderWithTexture(accessory.getTexture(), matrices, vertexConsumers, light, overlay, 1, 1, 1, 1);
        }

        model.setBodyVisible(true);
    }
}
