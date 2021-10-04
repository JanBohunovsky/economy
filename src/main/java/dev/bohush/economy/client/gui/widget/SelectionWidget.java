package dev.bohush.economy.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class SelectionWidget<T extends Enum<T>> extends DrawableHelper implements Drawable, Element, Selectable {
    private static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/widget/selection_background.png");

    public boolean visible = true;
    public int x;
    public int y;

    private final int width;
    private final int height;
    private Text text;

    private int index;
    private final List<Optional<T>> values;
    private final Function<T, Text> valueToText;
    private final UpdateCallback<T> updateCallback;

    private final ArrowButtonWidget leftButton;
    private final ArrowButtonWidget rightButton;

    public SelectionWidget(int x, int y, int width, T[] values, T initialValue, boolean allowNull, Function<T, Text> valueToText, UpdateCallback<T> updateCallback) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = ArrowButtonWidget.HEIGHT;

        this.leftButton = new ArrowButtonWidget(this.x, this.y, ArrowButtonWidget.Direction.LEFT, button -> {
            this.cycle(-1);
        });
        this.rightButton = new ArrowButtonWidget(this.x + this.width - ArrowButtonWidget.WIDTH, this.y, ArrowButtonWidget.Direction.RIGHT, button -> {
            this.cycle(1);
        });

        this.values = new ArrayList<>();
        if (allowNull) {
            this.values.add(Optional.empty());
        }

        for (var value : values) {
            this.values.add(Optional.of(value));
        }

        this.valueToText = valueToText;
        this.updateCallback = updateCallback;

        this.setValue(initialValue);
    }

    public int getWidth() {
        return this.width;
    }

    public T getValue() {
        return this.values.get(this.index).orElse(null);
    }

    public void setValue(T value) {
        int index = this.values.indexOf(Optional.ofNullable(value));
        if (index == -1) {
            return;
        }

        this.index = index;
        this.update();
    }

    private void update() {
        var value = this.getValue();
        if (value == null) {
            this.text = new TranslatableText("gui.selection_widget.none");
        } else {
            this.text = this.valueToText.apply(value);
        }

        this.leftButton.active = this.index > 0;
        this.rightButton.active = this.index < this.values.size() - 1;
    }

    private void cycle(int amount) {
        this.index += amount;
        if (this.index < 0) {
            this.index = 0;
        } else if (this.index >= this.values.size()) {
            this.index = this.values.size() - 1;
        }

        this.update();
        this.updateCallback.onValueChange(this, this.getValue());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!this.visible) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        int leftWidth = this.width / 2;
        int rightWidth = this.width - leftWidth;
        drawTexture(matrices, this.x, this.y, 0, 0, leftWidth, this.height, 200, 15);
        drawTexture(matrices, this.x + leftWidth, this.y, 200 - rightWidth, 0, rightWidth, this.height, 200, 15);

        this.leftButton.render(matrices, mouseX, mouseY, delta);
        this.rightButton.render(matrices, mouseX, mouseY, delta);

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int textOffsetX = (this.width - textRenderer.getWidth(this.text)) / 2;
        int textOffsetY = 4;
        textRenderer.drawWithShadow(matrices, this.text, this.x + textOffsetX, this.y + textOffsetY, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible) {
            return false;
        }

        return this.leftButton.mouseClicked(mouseX, mouseY, button)
            || this.rightButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!this.visible) {
            return false;
        }

        if (amount > 0) {
            this.cycle(-1);
        } else if (amount < 0) {
            this.cycle(1);
        }

        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width
            && mouseY >= this.y && mouseY < this.y + this.height;
    }

    @Override
    public SelectionType getType() {
        if (this.leftButton.getType() != SelectionType.NONE) {
            return this.leftButton.getType();
        }

        return this.rightButton.getType();
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }

    @Environment(EnvType.CLIENT)
    public interface UpdateCallback<T extends Enum<T>> {
        void onValueChange(SelectionWidget<T> widget, T value);
    }
}
