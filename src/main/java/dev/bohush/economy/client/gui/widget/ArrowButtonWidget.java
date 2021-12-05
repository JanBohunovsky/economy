package dev.bohush.economy.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ArrowButtonWidget extends BaseButtonWidget {
    public static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/widget/arrow_button.png");
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 64;
    public static final int WIDTH = 10;
    public static final int HEIGHT = 15;

    private final Direction direction;

    public ArrowButtonWidget(int x, int y, Direction direction, PressAction pressAction) {
        super(x, y, WIDTH, HEIGHT, pressAction);
        this.direction = direction;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        int v = !this.active ? 2
            : this.isHovered() ? 1
            : 0;

        this.drawTexture(matrices, this.x , this.y, this.direction.getU() * this.width, v * this.height, this.width, this.height);
    }

    @Override
    public void renderTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY) {
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Environment(EnvType.CLIENT)
    public enum Direction {
        LEFT(0),
        RIGHT(1);

        private final int u;

        private Direction(int u) {
            this.u = u;
        }

        public int getU() {
            return this.u;
        }
    }
}
