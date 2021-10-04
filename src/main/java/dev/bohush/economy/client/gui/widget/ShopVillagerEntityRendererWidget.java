package dev.bohush.economy.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3f;

public class ShopVillagerEntityRendererWidget implements Drawable {
    protected int x;
    protected int y;
    protected int size;
    protected LivingEntity entity;
    protected boolean followingMouse = true;

    private float rotation;

    public ShopVillagerEntityRendererWidget(int x, int y, int size, LivingEntity entity) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.entity = entity;
        this.resetRotation();
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSize() {
        return this.size;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public boolean isFollowingMouse() {
        return this.followingMouse;
    }

    public void setFollowingMouse(boolean followingMouse) {
        this.followingMouse = followingMouse;
        this.resetRotation();
    }

    public void resetRotation() {
        this.rotation = 180;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.entity.tick();

        if (!this.followingMouse) {
            this.rotation += delta * 2;
            if (this.rotation > 360) {
                this.rotation -= 360;
            }
        }

        // Don't ask me how this works, I took it from InventoryScreen.drawEntity

        float targetX = (float)Math.atan((this.x - mouseX) / 40f);
        float targetY = (float)Math.atan((this.y - (this.size * 9.5/6f) - mouseY) / 40f);

        var matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(this.x, this.y, 1050);
        matrixStack.scale(1, 1, -1);

        RenderSystem.applyModelViewMatrix();
        var matrixStack2 = new MatrixStack();
        matrixStack2.translate(0, 0, 1000);
        matrixStack2.scale(this.size, this.size, this.size);

        var quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180);
        var quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(targetY * 20);
        if (this.followingMouse) {
            quaternion.hamiltonProduct(quaternion2);
        }
        matrixStack2.multiply(quaternion);

        float oldBodyYaw = this.entity.bodyYaw;
        float oldYaw = this.entity.getYaw();
        float oldPitch = this.entity.getPitch();
        float oldPrevHeadYaw = this.entity.prevHeadYaw;
        float oldHeadYaw = this.entity.headYaw;

        if (this.followingMouse) {
            this.entity.bodyYaw = 180 + targetX * 20;
            this.entity.setYaw(180 + targetX * 40);
            this.entity.setPitch(targetY * -20);
        } else {
            this.entity.bodyYaw = this.rotation;
            this.entity.setYaw(this.rotation);
            this.entity.setPitch(0);
        }

        this.entity.headYaw = this.entity.getYaw();
        this.entity.prevHeadYaw = this.entity.getYaw();

        DiffuseLighting.method_34742();

        var entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);

        var immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(this.entity, 0, 0, 0, 0, 1, matrixStack2, immediate, 0xF000F0);
        });
        immediate.draw();

        entityRenderDispatcher.setRenderShadows(true);
        this.entity.bodyYaw = oldBodyYaw;
        this.entity.setYaw(oldYaw);
        this.entity.setPitch(oldPitch);
        this.entity.prevHeadYaw = oldPrevHeadYaw;
        this.entity.headYaw = oldHeadYaw;

        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();

        MinecraftClient.getInstance().textRenderer.draw(matrices, String.format("%.0f", this.rotation), 3, 3, 0xffffff);
    }
}
