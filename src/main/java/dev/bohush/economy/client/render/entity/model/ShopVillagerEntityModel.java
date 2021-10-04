package dev.bohush.economy.client.render.entity.model;

import dev.bohush.economy.entity.ShopVillagerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelWithHat;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ShopVillagerEntityModel extends SinglePartEntityModel<ShopVillagerEntity> implements ModelWithHead, ModelWithHat {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart hatRim;
    private final ModelPart nose;
    private final ModelPart body;
    private final ModelPart arms;
    private final ModelPart jacket;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ShopVillagerEntityModel(ModelPart root) {
        this.root = root;

        this.head = this.root.getChild(EntityModelPartNames.HEAD);
        this.hat = this.head.getChild(EntityModelPartNames.HAT);
        this.hatRim = this.hat.getChild(EntityModelPartNames.HAT_RIM);

        this.nose = this.head.getChild(EntityModelPartNames.NOSE);

        this.body = this.root.getChild(EntityModelPartNames.BODY);
        this.arms = this.root.getChild(EntityModelPartNames.ARMS);
        this.jacket = this.body.getChild(EntityModelPartNames.JACKET);
        this.rightLeg = this.root.getChild(EntityModelPartNames.RIGHT_LEG);
        this.leftLeg = this.root.getChild(EntityModelPartNames.LEFT_LEG);
    }

    public static TexturedModelData getTexturedModelData() {
        var modelData = new ModelData();
        var rootData = modelData.getRoot();

        var headData = rootData.addChild(
            EntityModelPartNames.HEAD,
            ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-4, -10, -4, 8, 10, 8),
            ModelTransform.NONE
        );

        var hatData = headData.addChild(
            EntityModelPartNames.HAT,
            ModelPartBuilder.create()
                .uv(32, 0)
                .cuboid(-4, -10, -4, 8, 10, 8, new Dilation(0.5f)),
            ModelTransform.NONE
        );

        hatData.addChild(
            EntityModelPartNames.HAT_RIM,
            ModelPartBuilder.create()
                .uv(30, 47)
                .cuboid(-8, -8, -6, 16, 16, 1),
            ModelTransform.rotation(-1.5707964f, 0, 0)
        );

        headData.addChild(
            EntityModelPartNames.NOSE,
            ModelPartBuilder.create()
                .uv(24, 0)
                .cuboid(-1, -1, -6, 2, 4, 2),
            ModelTransform.pivot(0, -2, 0)
        );

        var bodyData = rootData.addChild(
            EntityModelPartNames.BODY,
            ModelPartBuilder.create()
                .uv(16, 20)
                .cuboid(-4, 0, -3, 8, 12, 6),
            ModelTransform.NONE
        );

        bodyData.addChild(
            EntityModelPartNames.JACKET,
            ModelPartBuilder.create()
                .uv(0, 38)
                .cuboid(-4, 0, -3, 8, 20, 6, new Dilation(0.5f)),
            ModelTransform.NONE
        );

        rootData.addChild(
            EntityModelPartNames.ARMS,
            ModelPartBuilder.create()
                .uv(44, 22)
                .cuboid(-8, -2, -2, 4, 8, 4)
                .uv(44, 22)
                .cuboid(4, -2, -2, 4, 8, 4, true)
                .uv(40, 38)
                .cuboid(-4, 2, -2, 8, 4, 4),
            ModelTransform.of(0, 3, -1, -0.75f, 0, 0)
        );

        rootData.addChild(
            EntityModelPartNames.RIGHT_LEG,
            ModelPartBuilder.create()
                .uv(0, 22)
                .cuboid(-2, 0, -2, 4, 12, 4),
            ModelTransform.pivot(-2, 12, 0)
        );

        rootData.addChild(
            EntityModelPartNames.LEFT_LEG,
            ModelPartBuilder.create()
                .uv(0, 22)
                .cuboid(-2, 0, -2, 4, 12, 4),
            ModelTransform.pivot(2, 12, 0)
        );

        return TexturedModelData.of(modelData, 64, 64);
    }

    public void renderWithTexture(Identifier texture, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, float red, float green, float blue, float alpha) {
        var vertices = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
        this.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(ShopVillagerEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

        this.head.yaw = headYaw * 0.017453292f;
        this.head.pitch = headPitch * 0.017453292f;

        var headRolling = entity.getHeadRollingTimeLeft() > 0;
        if (headRolling) {
            this.head.roll = 0.3f * MathHelper.sin(0.45f * animationProgress);
            this.head.pitch = 0.4f;
        } else {
            this.head.roll = 0;
        }

        this.rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance * 0.5f;
        this.rightLeg.yaw = 0;

        this.leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + 3.1415927f) * 1.4f * limbDistance * 0.5f;
        this.leftLeg.yaw = 0;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void setHatVisible(boolean visible) {
        this.head.visible = visible;
        this.hat.visible = visible;
        this.hatRim.visible = visible;
    }

    public void setBodyVisible(boolean visible) {
        this.body.visible = visible;
        this.arms.visible = visible;
        this.jacket.visible = visible;
        this.rightLeg.visible = visible;
        this.leftLeg.visible = visible;
    }
}
