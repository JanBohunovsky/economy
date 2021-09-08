package dev.bohush.economy.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.client.gui.widget.OfferListWidget;
import dev.bohush.economy.screen.ShopCustomerScreenHandler;
import dev.bohush.economy.shop.ShopOffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ShopCustomerScreen extends HandledScreen<ShopCustomerScreenHandler> {
    protected static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/shop_customer.png");
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 256;
    protected static final Text OFFERS_TEXT = new TranslatableText("shop.offers");

    private int offersTitleX;

    public ShopCustomerScreen(ShopCustomerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 277;
        this.playerInventoryTitleX = 109;
    }

    @Override
    protected void init() {
        super.init();

        int titleWidth = this.textRenderer.getWidth(this.title);
        int titleOffset = 101;
        this.titleX = titleOffset + (this.backgroundWidth - titleOffset - titleWidth) / 2;

        int offersTextWidth = this.textRenderer.getWidth(OFFERS_TEXT);
        int offersOffset = 7;
        this.offersTitleX = offersOffset + (OfferListWidget.BACKGROUND_WIDTH - offersTextWidth) / 2;

        var offerListWidget = new OfferListWidget(this.x + 7, this.y + 17, this.handler.getOffers(),
            this::onOfferSelected,
            this.handler::getSelectedOffer);
        this.addDrawableChild(offerListWidget);
    }

    private void onOfferSelected(int offerIndex, ShopOffer offer) {
        if (client == null || client.player == null || client.interactionManager == null) {
            return;
        }

        handler.onButtonClick(client.player, offerIndex);
        client.interactionManager.clickButton(handler.syncId, offerIndex);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
        this.textRenderer.draw(matrices, OFFERS_TEXT, this.offersTitleX, 6, 0x404040);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        // Draw X over the middle arrow if the selected offer is disabled
        ShopOffer offer = this.handler.getSelectedOffer();
        if (offer != null && offer.isDisabled()) {
            RenderSystem.enableBlend();
            this.drawTexture(matrices, this.x + 190, this.y + 37, this.backgroundWidth, 0, 15, 15);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawDebugText(matrices);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    private void drawDebugText(MatrixStack matrices) {
        this.textRenderer.draw(matrices, Integer.toString(this.handler.getOfferIndex()), 3, 3, 0xffffff);
        if (this.handler.getSelectedOffer() != null) {
            ShopOffer offer = this.handler.getSelectedOffer();
            this.textRenderer.draw(matrices, String.format("%,d", offer.getSellItemStock()), 3, 13, 0xffffff);
            this.textRenderer.draw(matrices, String.format("%,d", offer.getAvailableSpaceForFirstItem()), 3, 23, 0xffffff);
            this.textRenderer.draw(matrices, String.format("%,d", offer.getAvailableSpaceForSecondItem()), 3, 33, 0xffffff);
        }
    }

    @Override
    protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
        super.drawMouseoverTooltip(matrices, x, y);

        // Render tooltip for the middle arrow if the offers is disabled
        if (!this.handler.getOffers().isEmpty()) {
            ShopOffer offer = this.handler.getSelectedOffer();

            if (offer != null && offer.isDisabled() && this.isPointWithinBounds(187, 34, 22, 21, x, y)) {
                this.renderTooltip(matrices, offer.getDisabledReasonText(), x, y);
            }
        }
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_HEIGHT, TEXTURE_WIDTH);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.getFocused() != null && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
