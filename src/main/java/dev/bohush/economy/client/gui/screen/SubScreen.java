package dev.bohush.economy.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class SubScreen extends AbstractParentElement implements Drawable {
    protected int x;
    protected int y;
    protected int backgroundWidth;
    protected int backgroundHeight;
    protected int width;
    protected int height;
    protected MinecraftClient client;
    protected ItemRenderer itemRenderer;
    protected TextRenderer textRenderer;

    private final List<Drawable> drawables = new ArrayList<>();
    private final List<Element> children = new ArrayList<>();

    protected SubScreen() {
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    /**
     * Initialize the sub-screen.
     * @param client Minecraft client
     * @param x This sub-screen's X position.
     * @param y This sub-screen's Y position.
     * @param width Main screen's width.
     * @param height Main screen's height.
     */
    public final void init(MinecraftClient client, int x, int y, int width, int height) {
        this.client = client;
        this.itemRenderer = this.client.getItemRenderer();
        this.textRenderer = this.client.textRenderer;
        this.width = width;
        this.height = height;

        this.clearChildren();
        this.setFocused(null);

        this.x = x;
        this.y = y;
        this.init();
    }

    /**
     * Called when the screen size changes or gets initialized for the first time.
     */
    protected void init() {
    }

    /**
     * Called when this sub-screen becomes visible.
     */
    public void enter() {
    }

    /**
     * Called when this sub-screen is about to be hidden.
     */
    public void leave() {
    }

    protected abstract void drawForeground(MatrixStack matrices, int mouseX, int mouseY);

    protected abstract void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY);

    protected abstract void drawMouseoverTooltip(Screen screen, MatrixStack matrices, int x, int y);

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.drawBackground(matrices, delta, mouseX, mouseY);
        for (var drawable : this.drawables) {
            drawable.render(matrices, mouseX, mouseY, delta);
        }

        var matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(this.x, this.y, 0);
        RenderSystem.applyModelViewMatrix();

        this.drawForeground(matrices, mouseX, mouseY);

        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();

        this.drawMouseoverTooltip(this.client.currentScreen, matrices, mouseX, mouseY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (var child : this.children) {
            if (child.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<? extends Element> children() {
        return this.children;
    }

    protected <T extends Drawable & Element> T addDrawableChild(T drawableElement) {
        this.addDrawable(drawableElement);
        return this.addChild(drawableElement);
    }

    protected <T extends Drawable> T addDrawable(T drawable) {
        this.drawables.add(drawable);
        return drawable;
    }

    protected <T extends Element> T addChild(T child) {
        this.children.add(child);
        return child;
    }

    protected void clearChildren() {
        this.drawables.clear();
        this.children.clear();
    }
}
