package dev.bohush.economy.client.gui.widget;

import dev.bohush.economy.client.gui.screen.SubScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TabWidget implements Drawable, Element, Selectable {
    private final List<SubScreen> subScreens = new ArrayList<>();
    private int selectedIndex;

    /**
     * Initialize all sub-screens.
     * @param client Minecraft client
     * @param x Sub-screen's X position.
     * @param y Sub-screen's Y position.
     * @param width Main screen's width.
     * @param height Main screen's height.
     */
    public void init(MinecraftClient client, int x, int y, int width, int height) {
        for (var subScreen : this.subScreens) {
            subScreen.init(client, x, y, width, height);
        }
    }

    public void add(SubScreen subScreen) {
        this.subScreens.add(subScreen);
    }

    public void add(int index, SubScreen subScreen) {
        this.subScreens.add(index, subScreen);
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public void setSelectedIndex(int index) {
        this.getSelectedSubScreen().ifPresent(SubScreen::leave);
        this.selectedIndex = index;
        this.getSelectedSubScreen().ifPresent(SubScreen::enter);
    }

    public Optional<SubScreen> getSelectedSubScreen() {
        if (this.selectedIndex < 0 || this.selectedIndex >= this.subScreens.size()) {
            return Optional.empty();
        }

        return Optional.of(this.subScreens.get(this.selectedIndex));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.getSelectedSubScreen().ifPresent(s -> s.render(matrices, mouseX, mouseY, delta));
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.getSelectedSubScreen().ifPresent(s -> s.mouseMoved(mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.getSelectedSubScreen()
            .map(s -> s.mouseClicked(mouseX, mouseY, button))
            .orElse(false);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.getSelectedSubScreen()
            .map(s -> s.mouseReleased(mouseX, mouseY, button))
            .orElse(false);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.getSelectedSubScreen()
            .map(s -> s.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            .orElse(false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.getSelectedSubScreen()
            .map(s -> s.mouseScrolled(mouseX, mouseY, amount))
            .orElse(false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.getSelectedSubScreen()
            .map(s -> s.keyPressed(keyCode, scanCode, modifiers))
            .orElse(false);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.getSelectedSubScreen()
            .map(s -> s.charTyped(chr, modifiers))
            .orElse(false);
    }

    @Override
    public boolean changeFocus(boolean lookForwards) {
        return this.getSelectedSubScreen()
            .map(s -> s.changeFocus(lookForwards))
            .orElse(false);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.getSelectedSubScreen()
            .map(s -> s.isMouseOver(mouseX, mouseY))
            .orElse(false);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }
}
