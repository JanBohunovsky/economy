package dev.bohush.economy.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.item.CoinPileItem;
import dev.bohush.economy.shop.ShopOffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class OfferStockBarWidget extends DrawableHelper implements Drawable, Element, Selectable {
    public static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/widget/offer_stock_bar.png");
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;
    public static final int WIDTH = 102;
    public static final int HEIGHT = 5;

    private static final int BORDER = 1;
    private static final int PROGRESS_WIDTH = WIDTH - BORDER * 2;

    public int x;
    public int y;

    private final SelectedOfferFunc selectedOfferFunc;

    public OfferStockBarWidget(int x, int y, SelectedOfferFunc selectedOfferFunc) {
        this.x = x;
        this.y = y;
        this.selectedOfferFunc = selectedOfferFunc;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);

        var offer = this.selectedOfferFunc.getSelectedOffer();

        if (offer == null || offer.isLocked() || offer.isStorageFull() || offer.getSellItem().isEmpty() || CoinPileItem.isCoinPile(offer.getSellItem())) {
            // Draw grey bar
            this.drawTexture(matrices, this.x, this.y, 0, 0, WIDTH, HEIGHT);
            return;
        }

        var scale = this.getStockScale(offer.getSellItem(), offer.getSellItemStock());
        var colorV = this.getBarColorV(scale);

        // Draw background
        this.drawTexture(matrices, this.x, this.y, 0, colorV, WIDTH, HEIGHT);

        int fillWidth = (int)(PROGRESS_WIDTH * scale);
        if (fillWidth > 0) {
            // Draw the fill level
            var width = BORDER + fillWidth + (scale == 1 ? BORDER : 0);
            this.drawTexture(matrices, this.x, this.y, 0, colorV + HEIGHT, width, HEIGHT);
        }

        if (this.isMouseOver(mouseX, mouseY)) {
            this.renderTooltip(MinecraftClient.getInstance().currentScreen, matrices, mouseX, mouseY, offer);
        }
    }

    private float getStockScale(ItemStack stack, int stock) {
        if (stock <= 0) {
            return 0;
        }

        var isStackable = stack.getCount() / stack.getMaxCount() < 1;
        var upperBound = isStackable ? 64f : 4f;
        var tradesLeft = (float)stock / stack.getCount();
        return MathHelper.clamp(tradesLeft / upperBound, 0.01f, 1f);
    }

    private int getBarColorV(float scale) {
        if (scale <= 0.25f) {
            return HEIGHT * 5;
        }

        if (scale <= 0.5f) {
            return HEIGHT * 3;
        }

        return HEIGHT;
    }

    public void renderTooltip(Screen screen, MatrixStack matrices, int mouseX, int mouseY, ShopOffer offer) {
        var tradesLeft = offer.getSellItemStock() / offer.getSellItem().getCount();
        var textTrades = new LiteralText(String.format("Trades left: %,d", tradesLeft));

        screen.renderTooltip(matrices, textTrades, mouseX, mouseY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + WIDTH
            && mouseY >= this.y && mouseY < this.y + HEIGHT;
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }

    @Environment(EnvType.CLIENT)
    public interface SelectedOfferFunc {
        @Nullable
        ShopOffer getSelectedOffer();
    }
}
