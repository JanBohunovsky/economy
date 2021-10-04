package dev.bohush.economy.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public abstract class BaseButtonWidget extends DrawableHelper implements Drawable, Element, Selectable {

    protected final PressAction pressAction;

    public int x;
    public int y;
    public boolean active = true;
    public boolean visible = true;

    protected int width;
    protected int height;

    protected boolean hovered;
    protected boolean focused;
    protected float alpha = 1;

    protected BaseButtonWidget(int x, int y, int width, int height, PressAction pressAction) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.pressAction = pressAction;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isHovered() {
        return this.hovered || this.focused;
    }

    public boolean isFocused() {
        return this.focused;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public abstract void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta);

    public abstract void renderTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY);

    /**
     * Called when the player successfully clicks the button.
     * By default, calls the PressAction that was passed through the constructor.
     */
    protected void onClick(double mouseX, double mouseY) {
        this.pressAction.onPress(this);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.hovered = false;

        if (this.visible) {
            this.hovered = this.active && this.isMouseOver(mouseX, mouseY);
            this.renderButton(matrices, mouseX, mouseY, delta);

            if (this.hovered) {
                var screen = MinecraftClient.getInstance().currentScreen;
                if (screen != null) {
                    this.renderTooltip(screen, matrices, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || !this.isMouseOver(mouseX, mouseY) || !this.isValidMouseButton(button)) {
            return false;
        }

        this.playClickSound(MinecraftClient.getInstance().getSoundManager());
        this.onClick(mouseX, mouseY);

        return true;
    }

    protected boolean isValidMouseButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width
            && mouseY >= this.y && mouseY < this.y + this.height;
    }

    public void playClickSound(SoundManager soundManager) {
        soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1));
    }

    @Override
    public SelectionType getType() {
        if (this.focused) {
            return SelectionType.FOCUSED;
        }

        return this.hovered
            ? SelectionType.HOVERED
            : SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(BaseButtonWidget button);
    }
}
