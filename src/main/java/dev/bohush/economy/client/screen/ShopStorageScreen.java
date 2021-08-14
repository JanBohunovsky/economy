package dev.bohush.economy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.screen.ShopStorageScreenHandler;
import dev.bohush.economy.util.CoinHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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

        // If we have more than one stack of the highest coin in the slot -> show the count as "64+"
        if (this.handler.getCoinValue() >= CoinHelper.getHighestCoin().getValue() * (CoinHelper.getHighestCoin().getMaxCount() + 1)) {
            // Slot for the highest coin
            Slot slot = this.handler.slots.get(CoinHelper.getCoinCount() - 1);
            this.textRenderer.drawWithShadow(matrices, "+", this.x + slot.x + 17, this.y + slot.y + 9, 0xffffff);
        }

        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}
