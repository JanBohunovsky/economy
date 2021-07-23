package urfriders.economy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import urfriders.economy.screen.ShopStorageScreenHandler;

public class ShopStorageScreen extends HandledScreen<ShopStorageScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/generic_54.png");

    public ShopStorageScreen(ShopStorageScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.backgroundHeight = 114 + 3 * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int startX = (width - backgroundWidth) / 2;
        int startY = (height - backgroundHeight) / 2;
//        drawTexture(matrices, startX, startY, 0, 0, backgroundWidth, backgroundHeight);
        drawTexture(matrices, startX, startY, 0, 0, this.backgroundWidth, 3 * 18 + 17);
        drawTexture(matrices, startX, startY + 3 * 18 + 17, 0, 126, this.backgroundWidth, 96);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}
