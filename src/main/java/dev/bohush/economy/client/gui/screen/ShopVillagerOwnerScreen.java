package dev.bohush.economy.client.gui.screen;

import dev.bohush.economy.Economy;
import dev.bohush.economy.screen.ShopVillagerOwnerScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ShopVillagerOwnerScreen extends HandledScreen<ShopVillagerOwnerScreenHandler> {
    protected static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/shop_villager.png");

    public ShopVillagerOwnerScreen(ShopVillagerOwnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
//        this.backgroundWidth = 100;
//        this.backgroundHeight = 100;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.setShaderTexture(0, TEXTURE);
//        int startX = (this.width - this.backgroundWidth) / 2;
//        int startY = (this.height - this.backgroundHeight) / 2;
//
//        drawTexture(matrices, startX, startY, this.getZOffset(), 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 512);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);

        var text = new LiteralText("Hello, this screen is not implemented yet.");
//        int centerX = (this.width - this.textRenderer.getWidth(text)) / 2;
//        int centerY = (this.height - this.textRenderer.fontHeight) / 2;
        this.textRenderer.draw(matrices, text, 0, 0, 0xffffff);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        // Your foreground stuff
    }
}
