package dev.bohush.economy.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.screen.ShopStorageScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ShopStorageScreen extends HandledScreen<ShopStorageScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/shop_storage.png");

    public ShopStorageScreen(ShopStorageScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.backgroundWidth = 204;
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int startX = (width - backgroundWidth) / 2;
        int startY = (height - backgroundHeight) / 2;
        drawTexture(matrices, startX, startY, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        this.textRenderer.draw(matrices, String.format("%,d", this.handler.getCoinValue()), 3, 3, 0xffffff);

        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}
