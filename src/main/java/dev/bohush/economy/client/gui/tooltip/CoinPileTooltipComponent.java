package dev.bohush.economy.client.gui.tooltip;

import dev.bohush.economy.item.CoinPileItem;
import dev.bohush.economy.item.CoinPileTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CoinPileTooltipComponent implements TooltipComponent {
    private static final int ITEM_SIZE = 16;
    private static final int PADDING = 4;
    private final List<ItemStack> coinStacks;

    public CoinPileTooltipComponent(CoinPileTooltipData data) {
        this.coinStacks = data.getCoinStacks();
    }

    @Override
    public int getHeight() {
        return ITEM_SIZE + PADDING;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        var count = this.coinStacks.size();
        return count * ITEM_SIZE + (count - 1) * PADDING;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z, TextureManager textureManager) {
        for (int i = 0; i < this.coinStacks.size(); i++) {
            var stack = this.coinStacks.get(i);
            var offsetX = i * (ITEM_SIZE + PADDING);
            var value = CoinPileItem.getValue(stack);
            var label = value == CoinPileItem.NETHERITE_COIN
                || value == CoinPileItem.GOLD_COIN
                || value == CoinPileItem.IRON_COIN
                || value == CoinPileItem.COPPER_COIN
                ? "1" : null;

            itemRenderer.renderInGuiWithOverrides(stack, x + offsetX, y);
            itemRenderer.renderGuiItemOverlay(textRenderer, stack, x + offsetX, y, label);
        }
    }
}
