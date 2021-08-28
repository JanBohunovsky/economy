package dev.bohush.economy.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.client.gui.widget.OfferListWidget;
import dev.bohush.economy.screen.ShopVillagerScreenHandler;
import dev.bohush.economy.shop.ShopOffer;
import dev.bohush.economy.shop.ShopOfferList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ShopVillagerScreen extends HandledScreen<ShopVillagerScreenHandler> {
    protected static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/shop_villager.png");
    protected static final Text OFFERS_TEXT = new TranslatableText("shop.offers");

    public ShopVillagerScreen(ShopVillagerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 276;
        this.playerInventoryTitleX = 107;
    }

    @Override
    protected void init() {
        super.init();
        int startX = (this.width - this.backgroundWidth) / 2;
        int startY = (this.height - this.backgroundHeight) / 2;

        if (this.handler.isOwner()) {
//            int x = startX + 101 - EditButtonWidget.SIZE;
            int x = startX + this.backgroundWidth - EditButtonWidget.SIZE - 4;
            int y = startY + 4;
            this.addDrawableChild(new EditButtonWidget(x, y));
        }

        var offerListWidget = new OfferListWidget(this, this.client, startX + 4, startY + 17, this.handler.getOffers(), this::onOfferSelected, this.handler::getSelectedOffer);
        this.addDrawableChild(offerListWidget);
    }

    private void onOfferSelected(int offerIndex, ShopOffer offer) {
        if (client == null || client.player == null || client.interactionManager == null) {
            return;
        }

        handler.onButtonClick(client.player, offerIndex);
        client.interactionManager.clickButton(handler.syncId, offerIndex);
    }

    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float) (49 + this.backgroundWidth / 2 - this.textRenderer.getWidth(this.title) / 2), 6.0F, 0x404040);
        this.textRenderer.draw(matrices, this.playerInventoryTitle, (float) this.playerInventoryTitleX, (float) this.playerInventoryTitleY, 0x404040);
        int textWidth = this.textRenderer.getWidth(OFFERS_TEXT);
        this.textRenderer.draw(matrices, OFFERS_TEXT, (float) (5 - textWidth / 2 + 48), 6, 0x404040);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int startX = (this.width - this.backgroundWidth) / 2;
        int startY = (this.height - this.backgroundHeight) / 2;

        drawTexture(matrices, startX, startY, this.getZOffset(), 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 512);

        // Draw X over the main arrow
        ShopOffer offer = this.handler.getSelectedOffer();
        if (offer != null && offer.isDisabled()) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexture(matrices, this.x + 83 + 99, this.y + 34, this.getZOffset(), 311.0F, 0.0F, 28, 21, 256, 512);
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        // Draw debug info in top left corner
        this.textRenderer.draw(matrices, Integer.toString(this.handler.getOfferIndex()), 3, 3, 0xffffff);
        if (this.handler.getSelectedOffer() != null) {
            ShopOffer offer = this.handler.getSelectedOffer();
            this.textRenderer.draw(matrices, String.format("%,d", offer.getSellItemStock()), 3, 13, 0xffffff);
            this.textRenderer.draw(matrices, String.format("%,d", offer.getAvailableSpaceForFirstItem()), 3, 23, 0xffffff);
            this.textRenderer.draw(matrices, String.format("%,d", offer.getAvailableSpaceForSecondItem()), 3, 33, 0xffffff);
        }

        // Render tooltip for main arrow
        ShopOfferList offers = this.handler.getOffers();
        if (!offers.isEmpty()) {
            ShopOffer offer = this.handler.getSelectedOffer();

            if (offer != null && offer.isDisabled() && this.isPointWithinBounds(186, 35, 22, 21, mouseX, mouseY)) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, TEXTURE);
                this.renderTooltip(matrices, offer.getDisabledReasonText(), mouseX, mouseY);
                RenderSystem.enableDepthTest();
            }
        }

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.getFocused() != null && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Environment(EnvType.CLIENT)
    public class EditButtonWidget extends ClickableWidget {
        public static final int SIZE = 11;

        public EditButtonWidget(int x, int y) {
            super(x, y, SIZE, SIZE, new TranslatableText("shop.offers.edit"));
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (client == null || client.player == null || client.interactionManager == null) {
                return;
            }

            handler.onButtonClick(client.player, ShopVillagerScreenHandler.EDIT_BUTTON_ID);
            client.interactionManager.clickButton(handler.syncId, ShopVillagerScreenHandler.EDIT_BUTTON_ID);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            int hoverOffset = this.isHovered() ? SIZE : 0;
            drawTexture(matrices, this.x, this.y, ShopVillagerScreen.this.backgroundWidth, hoverOffset, SIZE, SIZE, 512, 256);

            if (this.isHovered()) {
                this.renderTooltip(matrices, mouseX, mouseY);
            }
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
            ShopVillagerScreen.this.renderTooltip(matrices, this.getMessage(), mouseX, mouseY);
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }
}
