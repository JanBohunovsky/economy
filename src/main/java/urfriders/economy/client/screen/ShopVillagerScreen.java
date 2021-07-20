package urfriders.economy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import urfriders.economy.network.ModPackets;
import urfriders.economy.screen.ShopVillagerScreenHandler;

import java.util.Iterator;

// Code taken from MerchantScreen
@Environment(EnvType.CLIENT)
public class ShopVillagerScreen extends HandledScreen<ShopVillagerScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/villager2.png");
    private static final Text TRADES_TEXT = new TranslatableText("merchant.trades");
    private static final Text SEPARATOR_TEXT = new LiteralText(" - ");
    private static final Text DEPRECATED_TEXT = new LiteralText("Item is out of stock.");
    private int selectedIndex;
    private final WidgetButtonPage[] offers = new WidgetButtonPage[7];
    int indexStartOffset;
    private boolean scrolling;

    public ShopVillagerScreen(ShopVillagerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 276;
        this.playerInventoryTitleX = 107;
    }

    private void syncRecipeIndex() {
        this.handler.setRecipeIndex(this.selectedIndex);
        this.handler.switchTo(this.selectedIndex);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(this.selectedIndex);
        ClientPlayNetworking.send(ModPackets.SELECT_TRADE_C2S, buf);
    }

    protected void init() {
        super.init();
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        int k = j + 16 + 2;

        for(int l = 0; l < 7; ++l) {
            this.offers[l] = this.addDrawableChild(new WidgetButtonPage(i + 5, k, l, (button) -> {
                if (button instanceof WidgetButtonPage) {
                    this.selectedIndex = ((WidgetButtonPage)button).getIndex() + this.indexStartOffset;
                    this.syncRecipeIndex();
                }

            }));
            k += 20;
        }
    }

    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        int i = this.handler.getLevelProgress();
        if (i > 0 && i <= 5 && this.handler.isLeveled()) {
            Text text = this.title.shallowCopy().append(SEPARATOR_TEXT).append(new TranslatableText("merchant.level." + i));
            int j = this.textRenderer.getWidth(text);
            int k = 49 + this.backgroundWidth / 2 - j / 2;
            this.textRenderer.draw(matrices, text, (float)k, 6.0F, 4210752);
        } else {
            this.textRenderer.draw(matrices, this.title, (float)(49 + this.backgroundWidth / 2 - this.textRenderer.getWidth(this.title) / 2), 6.0F, 4210752);
        }

        this.textRenderer.draw(matrices, this.playerInventoryTitle, (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
        int l = this.textRenderer.getWidth(TRADES_TEXT);
        this.textRenderer.draw(matrices, TRADES_TEXT, (float)(5 - l / 2 + 48), 6.0F, 4210752);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        drawTexture(matrices, i, j, this.getZOffset(), 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 512);
        TradeOfferList tradeOfferList = this.handler.getRecipes();
        if (!tradeOfferList.isEmpty()) {
            int k = this.selectedIndex;
            if (k < 0 || k >= tradeOfferList.size()) {
                return;
            }

            TradeOffer tradeOffer = tradeOfferList.get(k);
            if (tradeOffer.isDisabled()) {
                RenderSystem.setShaderTexture(0, TEXTURE);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                drawTexture(matrices, this.x + 83 + 99, this.y + 35, this.getZOffset(), 311.0F, 0.0F, 28, 21, 256, 512);
            }
        }

    }

    private void drawLevelInfo(MatrixStack matrices, int x, int y, TradeOffer tradeOffer) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.handler.getLevelProgress();
        int j = this.handler.getExperience();
        if (i < 5) {
            drawTexture(matrices, x + 136, y + 16, this.getZOffset(), 0.0F, 186.0F, 102, 5, 256, 512);
            int k = VillagerData.getLowerLevelExperience(i);
            if (j >= k && VillagerData.canLevelUp(i)) {
                float f = 100.0F / (float)(VillagerData.getUpperLevelExperience(i) - k);
                int m = Math.min(MathHelper.floor(f * (float)(j - k)), 100);
                drawTexture(matrices, x + 136, y + 16, this.getZOffset(), 0.0F, 191.0F, m + 1, 5, 256, 512);
                int n = this.handler.getMerchantRewardedExperience();
                if (n > 0) {
                    int o = Math.min(MathHelper.floor((float)n * f), 100 - m);
                    drawTexture(matrices, x + 136 + m + 1, y + 16 + 1, this.getZOffset(), 2.0F, 182.0F, o, 3, 256, 512);
                }

            }
        }
    }

    private void renderScrollbar(MatrixStack matrices, int x, int y, TradeOfferList tradeOffers) {
        int i = tradeOffers.size() + 1 - 7;
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
        TradeOfferList tradeOfferList = this.handler.getRecipes();
        if (!tradeOfferList.isEmpty()) {
            int i = (this.width - this.backgroundWidth) / 2;
            int j = (this.height - this.backgroundHeight) / 2;
            int k = j + 16 + 1;
            int l = i + 5 + 5;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            this.renderScrollbar(matrices, i, j, tradeOfferList);
            int m = 0;
            Iterator var11 = tradeOfferList.iterator();

            while(true) {
                TradeOffer tradeOffer;
                while(var11.hasNext()) {
                    tradeOffer = (TradeOffer)var11.next();
                    if (this.canScroll(tradeOfferList.size()) && (m < this.indexStartOffset || m >= 7 + this.indexStartOffset)) {
                        ++m;
                    } else {
                        ItemStack itemStack = tradeOffer.getOriginalFirstBuyItem();
                        ItemStack itemStack2 = tradeOffer.getAdjustedFirstBuyItem();
                        ItemStack itemStack3 = tradeOffer.getSecondBuyItem();
                        ItemStack itemStack4 = tradeOffer.getSellItem();
                        this.itemRenderer.zOffset = 100.0F;
                        int n = k + 2;
                        this.renderFirstBuyItem(matrices, itemStack2, itemStack, l, n);
                        if (!itemStack3.isEmpty()) {
                            this.itemRenderer.renderInGui(itemStack3, i + 5 + 35, n);
                            this.itemRenderer.renderGuiItemOverlay(this.textRenderer, itemStack3, i + 5 + 35, n);
                        }

                        this.renderArrow(matrices, tradeOffer, i, n);
                        this.itemRenderer.renderInGui(itemStack4, i + 5 + 68, n);
                        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, itemStack4, i + 5 + 68, n);
                        this.itemRenderer.zOffset = 0.0F;
                        k += 20;
                        ++m;
                    }
                }

                int o = this.selectedIndex;
                tradeOffer = tradeOfferList.get(o);
                if (this.handler.isLeveled()) {
                    this.drawLevelInfo(matrices, i, j, tradeOffer);
                }

                if (tradeOffer.isDisabled() && this.isPointWithinBounds(186, 35, 22, 21, mouseX, mouseY) && this.handler.canRefreshTrades()) {
                    this.renderTooltip(matrices, DEPRECATED_TEXT, mouseX, mouseY);
                }

                WidgetButtonPage[] offers = this.offers;

                for (WidgetButtonPage widgetButtonPage : offers) {
                    if (widgetButtonPage.isHovered()) {
                        widgetButtonPage.renderTooltip(matrices, mouseX, mouseY);
                    }

                    widgetButtonPage.visible = widgetButtonPage.index < this.handler.getRecipes().size();
                }

                RenderSystem.enableDepthTest();
                break;
            }
        }

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    private void renderArrow(MatrixStack matrices, TradeOffer tradeOffer, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        if (tradeOffer.isDisabled()) {
            drawTexture(matrices, x + 5 + 35 + 20, y + 3, this.getZOffset(), 25.0F, 171.0F, 10, 9, 256, 512);
        } else {
            drawTexture(matrices, x + 5 + 35 + 20, y + 3, this.getZOffset(), 15.0F, 171.0F, 10, 9, 256, 512);
        }

    }

    private void renderFirstBuyItem(MatrixStack matrices, ItemStack adjustedFirstBuyItem, ItemStack originalFirstBuyItem, int x, int y) {
        this.itemRenderer.renderInGui(adjustedFirstBuyItem, x, y);
        if (originalFirstBuyItem.getCount() == adjustedFirstBuyItem.getCount()) {
            this.itemRenderer.renderGuiItemOverlay(this.textRenderer, adjustedFirstBuyItem, x, y);
        } else {
            this.itemRenderer.renderGuiItemOverlay(this.textRenderer, originalFirstBuyItem, x, y, originalFirstBuyItem.getCount() == 1 ? "1" : null);
            this.itemRenderer.renderGuiItemOverlay(this.textRenderer, adjustedFirstBuyItem, x + 14, y, adjustedFirstBuyItem.getCount() == 1 ? "1" : null);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            this.setZOffset(this.getZOffset() + 300);
            drawTexture(matrices, x + 7, y + 12, this.getZOffset(), 0.0F, 176.0F, 9, 2, 256, 512);
            this.setZOffset(this.getZOffset() - 300);
        }

    }

    private boolean canScroll(int listSize) {
        return listSize > 7;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int i = this.handler.getRecipes().size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.indexStartOffset = (int)((double)this.indexStartOffset - amount);
            this.indexStartOffset = MathHelper.clamp(this.indexStartOffset, 0, j);
        }

        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int i = this.handler.getRecipes().size();
        if (this.scrolling) {
            int j = this.y + 18;
            int k = j + 139;
            int l = i - 7;
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
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        if (this.canScroll(this.handler.getRecipes().size()) && mouseX > (double)(i + 94) && mouseX < (double)(i + 94 + 6) && mouseY > (double)(j + 18) && mouseY <= (double)(j + 18 + 139 + 1)) {
            this.scrolling = true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Environment(EnvType.CLIENT)
    class WidgetButtonPage extends ButtonWidget {
        final int index;

        public WidgetButtonPage(int x, int y, int index, PressAction onPress) {
            super(x, y, 89, 20, LiteralText.EMPTY, onPress);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
            if (this.hovered && ShopVillagerScreen.this.handler.getRecipes().size() > this.index + ShopVillagerScreen.this.indexStartOffset) {
                ItemStack itemStack3;
                if (mouseX < this.x + 20) {
                    itemStack3 = ShopVillagerScreen.this.handler.getRecipes().get(this.index + ShopVillagerScreen.this.indexStartOffset).getAdjustedFirstBuyItem();
                    ShopVillagerScreen.this.renderTooltip(matrices, itemStack3, mouseX, mouseY);
                } else if (mouseX < this.x + 50 && mouseX > this.x + 30) {
                    itemStack3 = ShopVillagerScreen.this.handler.getRecipes().get(this.index + ShopVillagerScreen.this.indexStartOffset).getSecondBuyItem();
                    if (!itemStack3.isEmpty()) {
                        ShopVillagerScreen.this.renderTooltip(matrices, itemStack3, mouseX, mouseY);
                    }
                } else if (mouseX > this.x + 65) {
                    itemStack3 = ShopVillagerScreen.this.handler.getRecipes().get(this.index + ShopVillagerScreen.this.indexStartOffset).getSellItem();
                    ShopVillagerScreen.this.renderTooltip(matrices, itemStack3, mouseX, mouseY);
                }
            }

        }
    }
}
