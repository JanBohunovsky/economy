package dev.bohush.economy.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.client.render.RenderSystemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SmallButtonWidget extends BaseButtonWidget {
    public static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/small_button.png");
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 16;
    public static final int HEIGHT = 13;

    private static final int HOVER_WIDTH = 3;
    private static final int HOVER_HEIGHT = HEIGHT + 2;
    private static final int HOVER_U = TEXTURE_WIDTH - HOVER_WIDTH;
    private static final int HOVER_V = 0;
    private static final int SYMBOL_SIZE = 9;

    private Text text;
    private Style style;
    @Nullable
    private Symbol symbol;

    public SmallButtonWidget(int x, int y, Text text, Style style, PressAction pressAction) {
        this(x, y, text, style, null, pressAction);
    }

    public SmallButtonWidget(int x, int y, Text text, Style style, @Nullable Symbol symbol, PressAction pressAction) {
        super(x, y, 0, HEIGHT, pressAction);
        this.text = text;
        this.style = style;
        this.symbol = symbol;

        this.width = MinecraftClient.getInstance().textRenderer.getWidth(text) + 5;
        if (this.symbol != null) {
            this.width += SYMBOL_SIZE;
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.pressAction.onPress(this);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        // Draw base
        int u = this.style.getU(this.active);
        int v = this.style.getV(this.active);
        this.drawTexture(matrices, this.x, this.y, u, v, 1, HEIGHT);
        drawTexture(matrices, this.x + 1, this.y, this.width - 2, HEIGHT, u + 1, v, 1, HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        this.drawTexture(matrices, this.x + this.width - 1, this.y, u + 2, v, 1, HEIGHT);

        // Draw symbol
        if (this.symbol != null) {
            RenderSystemUtils.setShaderColor(this.style.getSymbolColor(this.active), 1);
            this.drawTexture(matrices, this.x + 2, this.y + 2, this.symbol.getU(), this.symbol.getV(), SYMBOL_SIZE, SYMBOL_SIZE);
        }

        // Draw hover/focus
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (this.isHovered()) {
            this.drawTexture(matrices, this.x - 1, this.y - 1, HOVER_U, HOVER_V, 1, HOVER_HEIGHT);
            drawTexture(matrices, this.x, this.y - 1, this.width, HOVER_HEIGHT, HOVER_U + 1, HOVER_V, 1, HOVER_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            this.drawTexture(matrices, this.x + this.width, this.y - 1, HOVER_U + 2, HOVER_V, 1, HOVER_HEIGHT);
        }

        // Draw text
        MinecraftClient.getInstance()
            .textRenderer
            .draw(matrices, this.text, this.x + 3 + (this.symbol != null ? 9 : 0), this.y + 3, this.active ? 0xFFFFFFFF : 0xFFA0A0A0);
    }

    @Override
    public void renderTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY) {
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_HEIGHT, TEXTURE_WIDTH);
    }

    @Environment(EnvType.CLIENT)
    public enum Style {
        DEFAULT(1, 0, 0xC6C6C6),
        SUCCESS(2, 0, 0x80B847),
        DANGER(3, 0, 0xBF3A36);

        private static final int GRID_WIDTH = 3;
        private static final int GRID_HEIGHT = HEIGHT;

        private final int x;
        private final int y;
        private final int symbolColor;

        Style(int x, int y, int symbolColor) {
            this.x = x;
            this.y = y;
            this.symbolColor = symbolColor;
        }

        public int getU(boolean isActive) {
            return isActive ? this.x * GRID_WIDTH : 0;
        }

        public int getV(boolean isActive) {
            return isActive ? this.y * GRID_HEIGHT : 0;
        }

        public int getSymbolColor(boolean isActive) {
            return isActive ? this.symbolColor : 0xA0A0A0;
        }
    }

    @Environment(EnvType.CLIENT)
    public enum Symbol {
        CHECKMARK(0, 0),
        X(1, 0);

        private static final int GRID_OFFSET_X = 12;
        private static final int GRID_SIZE = SYMBOL_SIZE;

        private final int x;
        private final int y;

        Symbol(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getU() {
            return this.x * GRID_SIZE + GRID_OFFSET_X;
        }

        public int getV() {
            return this.y * GRID_SIZE;
        }
    }
}
