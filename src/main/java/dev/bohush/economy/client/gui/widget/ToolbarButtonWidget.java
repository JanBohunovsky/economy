package dev.bohush.economy.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ToolbarButtonWidget extends BaseButtonWidget {
    public static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/widget/toolbar_button.png");
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;
    public static final int SIZE = 9;

    private static final int HOVER_U = TEXTURE_WIDTH - SIZE - 2;
    private static final int HOVER_V = TEXTURE_HEIGHT - SIZE - 2;

    private final Style style;
    private final Symbol symbol;
    private final Text tooltipText;

    public ToolbarButtonWidget(int x, int y, Style style, Symbol symbol, Text tooltipText, PressAction pressAction) {
        super(x, y, SIZE, SIZE, pressAction);
        this.style = style;
        this.symbol = symbol;
        this.tooltipText = tooltipText;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        this.drawTexture(matrices, this.x, this.y, this.style.getU(this.active), this.style.getV(this.active));
        this.drawTexture(matrices, this.x, this.y, this.symbol.getU(this.active), this.symbol.getV(this.active));

        if (this.isHovered()) {
            this.drawTexture(matrices, this.x - 1, this.y - 1, HOVER_U, HOVER_V, SIZE + 2, SIZE + 2);
        }
    }

    @Override
    public void renderTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY) {
        screen.renderTooltip(matrices, this.tooltipText, mouseX, mouseY);
    }

    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v) {
        this.drawTexture(matrices, x, y, u, v, SIZE, SIZE);
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_HEIGHT, TEXTURE_WIDTH);
    }

    @Environment(EnvType.CLIENT)
    public enum Style {
        DEFAULT(1, 0),
        SUCCESS(2, 0),
        DANGER(3, 0);

        private final int x;
        private final int y;

        Style(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getU(boolean isActive) {
            return isActive ? this.x * SIZE : 0;
        }

        public int getV(boolean isActive) {
            return isActive ? this.y * SIZE : 0;
        }
    }

    @Environment(EnvType.CLIENT)
    public enum Symbol {
        PLUS(0, 1),
        MINUS(1, 1),
        ARROW_UP(2, 1),
        ARROW_DOWN(3, 1);

        private final int x;
        private final int y;

        Symbol(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getU(boolean isActive) {
            return this.x * SIZE;
        }

        public int getV(boolean isActive) {
            var y = this.y;
            if (!isActive) {
                y++;
            }

            return y * SIZE;
        }
    }
}
