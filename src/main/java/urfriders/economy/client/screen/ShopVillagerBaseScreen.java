package urfriders.economy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import urfriders.economy.screen.ShopVillagerBaseScreenHandler;
import urfriders.economy.shop.ShopOffer;
import urfriders.economy.shop.ShopOfferList;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public abstract class ShopVillagerBaseScreen<T extends ShopVillagerBaseScreenHandler> extends HandledScreen<T> {
    protected static final Identifier TEXTURE = new Identifier("textures/gui/container/villager2.png");
    protected static final Text OFFERS_TEXT = new TranslatableText("shop.offers");
    protected static final Text OUT_OF_STOCK_TEXT = new TranslatableText("shop.offer.outOfStock");
    protected static final Text DISABLED_TEXT = new TranslatableText("shop.offer.disabled");

    protected ArrayList<OfferButtonWidget> offerButtons = new ArrayList<>();
    protected int indexStartOffset;
    protected boolean scrolling;

    protected ShopVillagerBaseScreen(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 276;
        this.playerInventoryTitleX = 107;
    }

    protected void init() {
        super.init();
        int startX = (this.width - this.backgroundWidth) / 2;
        int startY = (this.height - this.backgroundHeight) / 2;

        for (int i = 0; i < 7; i++) {
            offerButtons.add(this.addDrawableChild(new OfferButtonWidget(startX + 5, i * 20 + startY + 16 + 2, i)));
        }
    }

    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float)(49 + this.backgroundWidth / 2 - this.textRenderer.getWidth(this.title) / 2), 6.0F, 4210752);
        this.textRenderer.draw(matrices, this.playerInventoryTitle, (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
        int textWidth = this.textRenderer.getWidth(OFFERS_TEXT);
        this.textRenderer.draw(matrices, OFFERS_TEXT, (float)(5 - textWidth / 2 + 48), 6.0F, 4210752);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int startX = (this.width - this.backgroundWidth) / 2;
        int startY = (this.height - this.backgroundHeight) / 2;

        drawTexture(matrices, startX, startY, this.getZOffset(), 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 512);

        ShopOfferList offers = this.handler.getOffers();
        if (!offers.isEmpty()) {
            int index = handler.getSelectedOffer();
            if (index < 0 || index >= offers.size()) {
                return;
            }

            ShopOffer offer = offers.get(index);
            if (offer.isDisabled()) {
                // Draw X over the main arrow
                RenderSystem.setShaderTexture(0, TEXTURE);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                drawTexture(matrices, this.x + 83 + 99, this.y + 35, this.getZOffset(), 311.0F, 0.0F, 28, 21, 256, 512);
            }
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

        ShopOfferList offers = this.handler.getOffers();
        if (!offers.isEmpty()) {
            int startX = (this.width - this.backgroundWidth) / 2;
            int startY = (this.height - this.backgroundHeight) / 2;
            int buttonY = startY + 16 + 1;
            int itemX = startX + 5 + 5;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            this.renderScrollbar(matrices, startX, startY, offers);
            int index = 0;

            for (ShopOffer offer : offers) {
                if (this.canScroll(offers.size()) && (index < this.indexStartOffset || index >= 7 + this.indexStartOffset)) {
                    // Offer is outside visible area -> skip it
                    index++;
                    continue;
                }

                ItemStack firstBuyItem = offer.getFirstBuyItem();
                ItemStack secondBuyItem = offer.getSecondBuyItem();
                ItemStack sellItem = offer.getSellItem();

                this.itemRenderer.zOffset = 100.0F;
                int itemY = buttonY + 2;

                // First item
                this.itemRenderer.renderInGui(firstBuyItem, itemX, itemY);
                this.itemRenderer.renderGuiItemOverlay(this.textRenderer, firstBuyItem, itemX, itemY);

                // Second item
                if (!secondBuyItem.isEmpty()) {
                    this.itemRenderer.renderInGui(secondBuyItem, startX + 5 + 35, itemY);
                    this.itemRenderer.renderGuiItemOverlay(this.textRenderer, secondBuyItem, startX + 5 + 35, itemY);
                }

                this.renderArrow(matrices, offer, startX, itemY);

                // Third item
                this.itemRenderer.renderInGui(sellItem, startX + 5 + 68, itemY);
                this.itemRenderer.renderGuiItemOverlay(this.textRenderer, sellItem, startX + 5 + 68, itemY);

                this.itemRenderer.zOffset = 0.0F;
                buttonY += 20;
                index++;
            }

            // Render tooltip for main arrow
            ShopOffer offer = offers.get(handler.getSelectedOffer());
            if (offer.isDisabled() && this.isPointWithinBounds(186, 35, 22, 21, mouseX, mouseY)) {
                Text text = offer.isOutOfStock()
                    ? OUT_OF_STOCK_TEXT
                    : DISABLED_TEXT;
                this.renderTooltip(matrices, text, mouseX, mouseY);
            }

            // Render tooltip for offer items and update visibility
            for (OfferButtonWidget offerButton : this.offerButtons) {
                if (offerButton.isHovered()) {
                    offerButton.renderTooltip(matrices, mouseX, mouseY);
                }

                offerButton.visible = offerButton.index < offers.size();
            }

            RenderSystem.enableDepthTest();
        }

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    private void renderArrow(MatrixStack matrices, ShopOffer offer, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        if (offer.isDisabled()) {
            drawTexture(matrices, x + 5 + 35 + 20, y + 3, this.getZOffset(), 25.0F, 171.0F, 10, 9, 256, 512);
        } else {
            drawTexture(matrices, x + 5 + 35 + 20, y + 3, this.getZOffset(), 15.0F, 171.0F, 10, 9, 256, 512);
        }
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
    public class OfferButtonWidget extends ButtonWidget {
        final int index;

        public OfferButtonWidget(int x, int y, int index) {
            super(x, y, 89, 20, LiteralText.EMPTY, null);
            this.index = index;
            this.visible = false;
        }

        @Override
        public void onPress() {
            handler.onButtonClick(client.player, index);
            client.interactionManager.clickButton(handler.syncId, index);
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
            if (this.hovered && handler.getOffers().size() > index + indexStartOffset) {
                ShopOffer offer = handler.getOffers().get(index + indexStartOffset);
                ItemStack item = ItemStack.EMPTY;
                if (mouseX < x + 20) {
                    item = offer.getFirstBuyItem();
                } else if (mouseX < x + 50 && mouseX > x + 30) {
                    item = offer.getSecondBuyItem();
                } else if (mouseX > x + 65) {
                    item = offer.getSellItem();
                }

                if (offer.isDisabled() && mouseX >= x + 53 && mouseX <= x + 65) {
                    Text text = offer.isOutOfStock()
                        ? OUT_OF_STOCK_TEXT
                        : DISABLED_TEXT;
                    ShopVillagerBaseScreen.this.renderTooltip(matrices, text, mouseX, mouseY);
                }

                if (!item.isEmpty()) {
                    ShopVillagerBaseScreen.this.renderTooltip(matrices, item, mouseX, mouseY);
                }
            }
        }
    }
}
