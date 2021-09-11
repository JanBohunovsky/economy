package dev.bohush.economy.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

@Environment(EnvType.CLIENT)
public class OfferLockButtonWidget extends BaseButtonWidget {
    public static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/lock_button.png");
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;
    public static final int WIDTH = 12;
    public static final int HEIGHT = 17;

    private boolean locked;

    public OfferLockButtonWidget(int x, int y, PressAction pressAction) {
        super(x, y, WIDTH, HEIGHT, pressAction);
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        // TODO: decide then remove
        boolean simple_style = false;

        int u;
        int v;

        if (!this.active) {
            v = HEIGHT;
        } else if (this.isHovered()) {
            v = 2 * HEIGHT;
        } else {
            v = 0;
        }

        if (this.locked) {
            u = 0;
        } else if (simple_style) {
            u = WIDTH;
        } else {
            u = 2 * WIDTH;
        }

        this.drawTexture(matrices, this.x, this.y, u, v, this.width + (!simple_style && !this.locked ? 4 : 0), this.height);
    }

    @Override
    public void renderTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY) {
        var title = new LiteralText(this.locked ? "Unlock offer" : "Lock offer");
        var description = new LiteralText("Players cannot use locked offers.").formatted(Formatting.DARK_GRAY);

        screen.renderTooltip(matrices, List.of(title, description), mouseX, mouseY);
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_HEIGHT, TEXTURE_WIDTH);
    }
}
