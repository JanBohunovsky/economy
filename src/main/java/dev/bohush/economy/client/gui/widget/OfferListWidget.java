package dev.bohush.economy.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.shop.ShopOffer;
import dev.bohush.economy.shop.ShopOfferList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class OfferListWidget extends AbstractParentElement implements Drawable, Element, Selectable {
    public static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/shop_offer_list.png");
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 256;
    public static final int BACKGROUND_WIDTH = 97;
    public static final int BACKGROUND_HEIGHT = 142;
    public static final int BUTTON_COUNT = 7;

    private static final int PADDING = 1;
    private static final int WIDTH = BACKGROUND_WIDTH - PADDING * 2;
    private static final int HEIGHT = BACKGROUND_HEIGHT - PADDING * 2;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_MIN_SIZE = 3;
    private static final int SCROLLBAR_MAX_SIZE = 68;
    private static final int SCROLLBAR_X = PADDING + WIDTH - SCROLLBAR_WIDTH;
    private static final int SCROLLBAR_Y = PADDING;

    public int x;
    public int y;

    private int indexOffset;
    private int scrollbarHeight;
    private boolean scrolling;

    private final OfferSelectedAction offerSelectedAction;
    private final SelectedOfferFunc selectedOfferFunc;
    private final ShopOfferList offers;
    private final ArrayList<OfferButtonWidget> buttons;

    public OfferListWidget(int x, int y, ShopOfferList offers,
                           OfferSelectedAction offerSelectedAction, SelectedOfferFunc selectedOfferFunc) {
        this.x = x;
        this.y = y;
        this.offerSelectedAction = offerSelectedAction;
        this.selectedOfferFunc = selectedOfferFunc;
        this.offers = offers;
        this.buttons = new ArrayList<>();

        for (int i = 0; i < BUTTON_COUNT; i++) {
            var button = new OfferButtonWidget(this.x + PADDING, this.y + PADDING + (OfferButtonWidget.HEIGHT * i), i, this::onButtonClick);
            this.buttons.add(button);
        }
    }

    @Override
    public List<? extends Element> children() {
        return this.buttons;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matrices, this.x, this.y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        double count = Math.max(this.offers.size(), BUTTON_COUNT);
        this.scrollbarHeight = (int) (BUTTON_COUNT / count * (HEIGHT - 1));
        if (this.scrollbarHeight % 2 == 0) {
            this.scrollbarHeight--;
        }

        this.renderScrollbar(matrices);

        if (this.offers.isEmpty()) {
            return;
        }

        var selectedOffer = this.selectedOfferFunc.getSelectedOffer();
        for (var button : this.buttons) {
            var index = this.indexOffset + button.getButtonIndex();
            var offer = index < this.offers.size()
                ? this.offers.get(index)
                : null;

            button.setOffer(offer);
            button.setSelected(selectedOffer != null && offer == selectedOffer);
            button.render(matrices, mouseX, mouseY, delta);
        }
    }

    private boolean canScroll() {
        return this.offers.size() > BUTTON_COUNT;
    }

    private void renderScrollbar(MatrixStack matrices) {
        int innerScrollbarHeight = (this.scrollbarHeight - 3) / 2;
        int size = MathHelper.clamp(innerScrollbarHeight, SCROLLBAR_MIN_SIZE, SCROLLBAR_MAX_SIZE);

        int startX = this.x + SCROLLBAR_X;
        int startY = this.y + SCROLLBAR_Y;

        if (this.canScroll()) {
            int availableHeight = HEIGHT - this.scrollbarHeight;
            int steps = this.offers.size() - BUTTON_COUNT;
            int heightPerStep = availableHeight / steps;

            startY += this.indexOffset * heightPerStep;

            // Align scrollbar with the bottom if we're at the bottom of the list.
            // This is needed because the scrollbar's height is always odd.
            if (this.indexOffset == steps) {
                startY++;
            }
        }

        int u = BACKGROUND_WIDTH;
        if (!this.canScroll()) {
            u += SCROLLBAR_WIDTH;
        }

        // Top
        drawTexture(matrices, startX, startY, u, 0, SCROLLBAR_WIDTH, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // Middle
        for (int i = 1; i <= size; i++) {
            drawTexture(matrices, startX, startY + (2 * i), u, 2, SCROLLBAR_WIDTH, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        // Bottom
        drawTexture(matrices, startX, startY + (size * 2) + 2, u, 4, SCROLLBAR_WIDTH, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.canScroll()) {
            int extraOfferCount = this.offers.size() - BUTTON_COUNT;
            this.indexOffset = (int) ((double) this.indexOffset - amount);
            this.indexOffset = MathHelper.clamp(this.indexOffset, 0, extraOfferCount);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!this.scrolling) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        int steps = this.offers.size() - BUTTON_COUNT;
        int availableSpace = HEIGHT - this.scrollbarHeight;
        float mouseOffsetY = (float)mouseY - this.y - SCROLLBAR_Y - this.scrollbarHeight / 2.0f;

        float scrollPercent = mouseOffsetY / availableSpace;
        int scrollIndexOffset = (int)(scrollPercent * steps + 0.5f);

        this.indexOffset = MathHelper.clamp(scrollIndexOffset, 0, steps);

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;

        if (this.canScroll()
            && mouseX >= this.x + SCROLLBAR_X && mouseX < this.x + SCROLLBAR_X + SCROLLBAR_WIDTH
            && mouseY >= this.y + SCROLLBAR_Y && mouseY < this.y + SCROLLBAR_Y + HEIGHT) {
            this.scrolling = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + BACKGROUND_WIDTH
            && mouseY >= this.y && mouseY < this.y + BACKGROUND_HEIGHT;
    }

    private void onButtonClick(OfferButtonWidget button) {
        int offerIndex = button.getButtonIndex() + this.indexOffset;
        var offer = this.offers.get(offerIndex);

        this.offerSelectedAction.onOfferSelected(offerIndex, offer);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }

    @Environment(EnvType.CLIENT)
    public interface OfferSelectedAction {
        void onOfferSelected(int offerIndex, ShopOffer offer);
    }

    @Environment(EnvType.CLIENT)
    public interface SelectedOfferFunc {
        @Nullable
        ShopOffer getSelectedOffer();
    }
}
