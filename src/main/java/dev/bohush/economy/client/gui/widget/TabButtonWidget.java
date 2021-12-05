package dev.bohush.economy.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TabButtonWidget extends BaseButtonWidget {
    public static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/widget/tab_button.png");
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 64;

    private static final int GRID_WIDTH = 8;

    private final int index;
    private final Position position;
    private final Text text;
    private final TextRenderer textRenderer;
    private final int textX;
    private final int textY;

    private boolean selected;

    public TabButtonWidget(int x, int y, int width, int index, int selectedIndex, Position position, Text text, PressAction pressAction) {
        super(x, y, Math.max(width, GRID_WIDTH), 21, pressAction);
        this.index = index;
        this.position = position;
        this.text = text;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;

        this.setSelected(selectedIndex);

        int textWidth = this.textRenderer.getWidth(this.text);
        int textHeight = this.textRenderer.fontHeight - 1;

        this.textX = (this.width - textWidth) / 2;
        this.textY = (int)Math.round((this.height - textHeight) / 2.0);
    }

    public int getIndex() {
        return this.index;
    }

    public void setSelected(int selectedIndex) {
        this.selected = this.index == selectedIndex;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int u = this.position.ordinal() * GRID_WIDTH;
        int v = this.selected ? this.height : 0;

        final int leftWidth = 4;
        final int rightWidth = 3;

        this.drawTexture(matrices, this.x, this.y, u, v, leftWidth, this.height);
        drawTexture(matrices, this.x + leftWidth, this.y, this.width - leftWidth - rightWidth, this.height, u + leftWidth, v, 1, this.height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        this.drawTexture(matrices, this.x + this.width - rightWidth, this.y, u + GRID_WIDTH - rightWidth, v, rightWidth, this.height);

        this.textRenderer.draw(matrices, this.text, this.x + this.textX, this.y + this.textY + (this.selected ? 0 : 1), this.selected ? 0x404040 : 0x1C1C1C);
    }

    @Override
    public void renderTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY) {

    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width
            && mouseY >= this.y && mouseY < this.y + this.height - 4;
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public enum Position {
        LEFT,
        MIDDLE,
        RIGHT
    }
}
