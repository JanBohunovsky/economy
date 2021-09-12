package dev.bohush.economy.client.gui.widget;


import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.shop.ShopOffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class OfferButtonWidget extends PressableWidget {
    public static final int WIDTH = 88;
    public static final int HEIGHT = 20;

    private final int buttonIndex;
    private final boolean ownerView;
    private final PressAction pressAction;

    private boolean selected;
    @Nullable
    private ShopOffer offer;

    public OfferButtonWidget(int x, int y, int buttonIndex, boolean ownerView, PressAction pressAction) {
        super(x, y, WIDTH, HEIGHT, LiteralText.EMPTY);
        this.buttonIndex = buttonIndex;
        this.ownerView = ownerView;
        this.pressAction = pressAction;
        this.visible = false;
    }

    public int getButtonIndex() {
        return this.buttonIndex;
    }

    public void setOffer(@Nullable ShopOffer offer) {
        this.offer = offer;
        this.visible = offer != null;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onPress() {
        this.pressAction.onPress(this);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        if (this.offer == null) {
            return;
        }

        this.renderItem(this.offer.getFirstBuyItem(), 5);
        this.renderItem(this.offer.getSecondBuyItem(), 35);
        this.renderArrow(matrices);
        this.renderItem(offer.getSellItem(), 68);

        if (this.hovered) {
            this.renderTooltip(matrices, mouseX, mouseY);
        }
    }

    private void renderItem(ItemStack stack, int x) {
        if (stack.isEmpty()) {
            return;
        }

        var client = MinecraftClient.getInstance();
        var itemRenderer = client.getItemRenderer();
        itemRenderer.zOffset = 100;

        itemRenderer.renderInGui(stack, this.x + x, this.y + 1);
        itemRenderer.renderGuiItemOverlay(client.textRenderer, stack, this.x + x, this.y + 1);

        itemRenderer.zOffset = 0;
    }

    private void renderArrow(MatrixStack matrices) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, OfferListWidget.TEXTURE);

        int u = OfferListWidget.BACKGROUND_WIDTH;
        if (this.showX()) {
            u += 10;
        }

        drawTexture(matrices, this.x + 55, this.y + 4, this.getZOffset(), u, 5, 10, 9, OfferListWidget.TEXTURE_HEIGHT, OfferListWidget.TEXTURE_WIDTH);
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if (this.offer == null) {
            return;
        }

        var hoveredItemStack = ItemStack.EMPTY;

        if (mouseX >= this.x + 5 && mouseX < this.x + 21) {
            hoveredItemStack = this.offer.getFirstBuyItem();
        } else if (mouseX >= this.x + 35 && mouseX < this.x + 51) {
            hoveredItemStack = this.offer.getSecondBuyItem();
        } else if (mouseX >= this.x + 68 && mouseX < this.x + 84) {
            hoveredItemStack = this.offer.getSellItem();
        }

        var screen = MinecraftClient.getInstance().currentScreen;
        if (screen == null) {
            return;
        }

        if (this.showX() && mouseX >= this.x + 52 && mouseX < this.x + 68) {
            screen.renderTooltip(matrices, offer.getDisabledReasonText(), mouseX, mouseY);
        }

        if (!hoveredItemStack.isEmpty()) {
            screen.renderTooltip(matrices, screen.getTooltipFromItem(hoveredItemStack), hoveredItemStack.getTooltipData(), mouseX, mouseY);
        }
    }

    private boolean showX() {
        if (this.offer == null) {
            return false;
        }

        return this.ownerView ? this.offer.isLocked() : this.offer.isDisabled();
    }

    @Override
    protected int getYImage(boolean hovered) {
        if (this.selected) {
            return 2;
        }

        return super.getYImage(hovered);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction {
        void onPress(OfferButtonWidget button);
    }
}
