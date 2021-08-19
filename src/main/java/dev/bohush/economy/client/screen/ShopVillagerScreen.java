package dev.bohush.economy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.screen.ShopVillagerScreenHandler;
import dev.bohush.economy.shop.ShopOffer;
import dev.bohush.economy.shop.ShopOfferList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class ShopVillagerScreen extends HandledScreen<ShopVillagerScreenHandler> {
    protected static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/shop_villager.png");
    protected static final Text OFFERS_TEXT = new TranslatableText("shop.offers");

    protected ArrayList<OfferButtonWidget> offerButtons = new ArrayList<>();
    protected int indexStartOffset;
    protected boolean scrolling;

    public ShopVillagerScreen(ShopVillagerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 276;
        this.playerInventoryTitleX = 107;
    }

    protected void init() {
        super.init();
        int startX = (this.width - this.backgroundWidth) / 2;
        int startY = (this.height - this.backgroundHeight) / 2;

        if (this.handler.isOwner()) {
            int x = startX + 101 - EditButtonWidget.SIZE;
            int y = startY + 4;
            this.addDrawableChild(new EditButtonWidget(x, y));
        }

        for (int i = 0; i < 7; i++) {
            offerButtons.add(this.addDrawableChild(new OfferButtonWidget(startX + 5, i * 20 + startY + 16 + 2, i)));
        }
    }

    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float)(49 + this.backgroundWidth / 2 - this.textRenderer.getWidth(this.title) / 2), 6.0F, 0x404040);
        this.textRenderer.draw(matrices, this.playerInventoryTitle, (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 0x404040);
        int textWidth = this.textRenderer.getWidth(OFFERS_TEXT);
        this.textRenderer.draw(matrices, OFFERS_TEXT, (float)(5 - textWidth / 2 + 48), 6.0F, 0x404040);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int startX = (this.width - this.backgroundWidth) / 2;
        int startY = (this.height - this.backgroundHeight) / 2;

        drawTexture(matrices, startX, startY, this.getZOffset(), 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 512);

        ShopOffer offer = this.handler.getSelectedOffer();
        if (offer != null && offer.isDisabled()) {
            // Draw X over the main arrow
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexture(matrices, this.x + 83 + 99, this.y + 35, this.getZOffset(), 311.0F, 0.0F, 28, 21, 256, 512);
        }
    }

    private void renderScrollbar(MatrixStack matrices, int x, int y, ShopOfferList offers) {
        int i = offers.size() + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int m = Math.min(113, this.indexStartOffset * k);
            if (this.indexStartOffset == i - 1) {
                m = 113;
            }

            drawTexture(matrices, x + 94, y + 18 + m, this.getZOffset(), 0.0F, 199.0F, 6, 27, 256, 512);
        } else {
            drawTexture(matrices, x + 94, y + 18, this.getZOffset(), 6.0F, 199.0F, 6, 27, 256, 512);
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.textRenderer.draw(matrices, Integer.toString(this.handler.getOfferIndex()), 3, 3, 0xffffff);
        if (this.handler.getSelectedOffer() != null) {
            ShopOffer offer = this.handler.getSelectedOffer();
            this.textRenderer.draw(matrices, String.format("%,d", offer.getSellItemStock()), 3, 13, 0xffffff);
            this.textRenderer.draw(matrices, String.format("%,d", offer.getAvailableSpaceForFirstItem()), 3, 23, 0xffffff);
            this.textRenderer.draw(matrices, String.format("%,d", offer.getAvailableSpaceForSecondItem()), 3, 33, 0xffffff);
        }

        ShopOfferList offers = this.handler.getOffers();
        if (!offers.isEmpty()) {
            int startX = (this.width - this.backgroundWidth) / 2;
            int startY = (this.height - this.backgroundHeight) / 2;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            this.renderScrollbar(matrices, startX, startY, offers);

            // Render tooltip for main arrow
            ShopOffer offer = this.handler.getSelectedOffer();
            if (offer != null && offer.isDisabled() && this.isPointWithinBounds(186, 35, 22, 21, mouseX, mouseY)) {
                this.renderTooltip(matrices, offer.getDisabledReasonText(), mouseX, mouseY);
            }

            // Render tooltips for buttons
            for (OfferButtonWidget offerButton : this.offerButtons) {
                offerButton.visible = offerButton.index < offers.size();

                if (offerButton.isHovered()) {
                    offerButton.renderTooltip(matrices, mouseX, mouseY);
                }
            }

            RenderSystem.enableDepthTest();
        }

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    private boolean canScroll(int listSize) {
        return listSize > 7;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int offerCount = this.handler.getOffers().size();
        if (this.canScroll(offerCount)) {
            int j = offerCount - 7;
            this.indexStartOffset = (int)((double)this.indexStartOffset - amount);
            this.indexStartOffset = MathHelper.clamp(this.indexStartOffset, 0, j);
        }

        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int offerCount = this.handler.getOffers().size();
        if (this.scrolling) {
            int j = this.y + 18;
            int k = j + 139;
            int l = offerCount - 7;
            float f = ((float)mouseY - (float)j - 13.5F) / ((float)(k - j) - 27.0F);
            f = f * (float)l + 0.5F;
            this.indexStartOffset = MathHelper.clamp((int)f, 0, l);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        int startX = (this.width - this.backgroundWidth) / 2;
        int startY = (this.height - this.backgroundHeight) / 2;

        if (this.canScroll(this.handler.getOffers().size()) && mouseX > (double)(startX + 94) && mouseX < (double)(startX + 94 + 6) && mouseY > (double)(startY + 18) && mouseY <= (double)(startY + 18 + 139 + 1)) {
            this.scrolling = true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Environment(EnvType.CLIENT)
    public class OfferButtonWidget extends PressableWidget {
        private final int index;
        private boolean isSelected;

        public OfferButtonWidget(int x, int y, int index) {
            super(x, y, 89, 20, LiteralText.EMPTY);
            this.index = index;
            this.visible = false;
        }

        @Override
        public void onPress() {
            if (client == null || client.player == null || client.interactionManager == null) {
                return;
            }

            handler.onButtonClick(client.player, indexStartOffset + this.index);
            client.interactionManager.clickButton(handler.syncId, indexStartOffset + this.index);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.isSelected = handler.getOfferIndex() == indexStartOffset + this.index;

            super.renderButton(matrices, mouseX, mouseY, delta);
            this.renderOffer(matrices);
        }

        private void renderOffer(MatrixStack matrices) {
            itemRenderer.zOffset = 100.0F;

            ShopOffer offer = handler.getOffers().get(indexStartOffset + this.index);
            this.renderItem(offer.getFirstBuyItem(), 5);

            if (!offer.getSecondBuyItem().isEmpty()) {
                this.renderItem(offer.getSecondBuyItem(), 35);
            }

            this.renderArrow(matrices, offer);
            this.renderItem(offer.getSellItem(), 68);

            itemRenderer.zOffset = 0.0F;
        }

        private void renderItem(ItemStack stack, int x) {
            itemRenderer.renderInGui(stack, this.x + x, this.y + 1);
            itemRenderer.renderGuiItemOverlay(textRenderer, stack, this.x + x, this.y + 1);
        }

        private void renderArrow(MatrixStack matrices, ShopOffer offer) {
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            if (offer.isDisabled()) {
                drawTexture(matrices, this.x + 55, this.y + 4, ShopVillagerScreen.this.getZOffset(), 25.0F, 171.0F, 10, 9, 256, 512);
            } else {
                drawTexture(matrices, this.x + 55, this.y + 4, ShopVillagerScreen.this.getZOffset(), 15.0F, 171.0F, 10, 9, 256, 512);
            }
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
            if (!this.hovered) {
                return;
            }

            ShopOfferList offers = handler.getOffers();
            if (indexStartOffset + this.index < offers.size()) {
                ShopOffer offer = offers.get(this.index + indexStartOffset);
                ItemStack item = ItemStack.EMPTY;
                if (mouseX < this.x + 20) {
                    item = offer.getFirstBuyItem();
                } else if (mouseX < this.x + 50 && mouseX > this.x + 30) {
                    item = offer.getSecondBuyItem();
                } else if (mouseX > this.x + 65) {
                    item = offer.getSellItem();
                }

                if (offer.isDisabled() && mouseX >= this.x + 53 && mouseX <= this.x + 65) {
                    ShopVillagerScreen.this.renderTooltip(matrices, offer.getDisabledReasonText(), mouseX, mouseY);
                }

                if (!item.isEmpty()) {
                    ShopVillagerScreen.this.renderTooltip(matrices, item, mouseX, mouseY);
                }
            }
        }

        @Override
        public boolean isHovered() {
            return super.isHovered() || this.isSelected;
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }

    @Environment(EnvType.CLIENT)
    public class EditButtonWidget extends ClickableWidget {
        public static final int SIZE = 11;

        public EditButtonWidget(int x, int y) {
            super(x, y, SIZE, SIZE, new TranslatableText("shop.offers.edit"));
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            super.onClick(mouseX, mouseY);
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
