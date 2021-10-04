package dev.bohush.economy.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ToggleButtonWidget extends BaseButtonWidget {
    private final static Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/widget/toggle_button.png");
    private final static int TEXTURE_WIDTH = 64;
    private final static int TEXTURE_HEIGHT = 32;
    private final static int WIDTH = 16;

    public boolean coloredOff;

    private final ToggleAction toggleAction;
    private final TextRenderer textRenderer;
    @Nullable
    private final Text text;
    private boolean on;

    public ToggleButtonWidget(int x, int y, @Nullable Text text, ToggleAction toggleAction) {
        super(x, y, WIDTH, 9, null);
        this.toggleAction = toggleAction;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.text = text;

        if (this.text == null) {
            return;
        }

        this.width = WIDTH + 3 + this.textRenderer.getWidth(this.text);
    }

    public boolean isOn() {
        return this.on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    protected void onClick(double mouseX, double mouseY) {
        this.on = !this.on;
        this.toggleAction.onToggle(this.on);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        int u = this.active ? 0 : 1;
        int v = this.on ? 0
            : this.coloredOff && this.active ? 2
            : 1;

        this.drawTexture(matrices, this.x, this.y, u * WIDTH, v * this.height, WIDTH, this.height);

        if (this.text == null) {
            return;
        }

        this.textRenderer.draw(matrices, this.text, this.x + WIDTH + 3, this.y + 1, 0x404040);
    }

    @Override
    public void renderTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY) {
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_HEIGHT, TEXTURE_WIDTH);
    }

    @Environment(EnvType.CLIENT)
    public interface ToggleAction {
        void onToggle(boolean on);
    }
}
