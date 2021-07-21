package urfriders.economy.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import urfriders.economy.entity.ShopVillagerEntity;

@Environment(EnvType.CLIENT)
public class ShopVillagerEntityRenderer extends MobEntityRenderer<ShopVillagerEntity, VillagerResemblingModel<ShopVillagerEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/villager/villager.png");

    public ShopVillagerEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new VillagerResemblingModel<>(context.getPart(EntityModelLayers.VILLAGER)), 0.5F);
        this.addFeature(new HeadFeatureRenderer<>(this, context.getModelLoader()));
        this.addFeature(new VillagerClothingFeatureRenderer<>(this, context.getResourceManager(), "villager"));
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
