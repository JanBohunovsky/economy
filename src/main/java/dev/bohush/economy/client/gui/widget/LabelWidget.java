package dev.bohush.economy.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class LabelWidget implements Drawable {
    public int x;
    public int y;
    public Text text;
    public int color;
    public HorizontalAlignment horizontalAlignment;

    private final TextRenderer textRenderer;

    public LabelWidget(int x, int y, Text text) {
        this(x, y, text, 0x404040, HorizontalAlignment.LEFT);
    }

    public LabelWidget(int x, int y, Text text, int color, HorizontalAlignment horizontalAlignment) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.horizontalAlignment = horizontalAlignment;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
    }

    public int getWidth() {
        return this.textRenderer.getWidth(this.text);
    }

    public int getHeight() {
        return this.textRenderer.fontHeight - 1;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        if (this.horizontalAlignment == HorizontalAlignment.LEFT) {
            textRenderer.draw(matrices, this.text, this.x, this.y, this.color);
            return;
        }

        int textWidth = textRenderer.getWidth(this.text);

        if (this.horizontalAlignment == HorizontalAlignment.RIGHT) {
            textRenderer.draw(matrices, this.text, this.x - textWidth, this.y, this.color);
            return;
        }

        if (this.horizontalAlignment == HorizontalAlignment.CENTER) {
            textRenderer.draw(matrices, this.text, this.x - textWidth / 2f, this.y, this.color);
        }
    }

    @Environment(EnvType.CLIENT)
    public enum HorizontalAlignment {
        LEFT,
        CENTER,
        RIGHT
    }
}
