package dev.bohush.economy.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

import java.util.List;

public class CoinPileTooltipData implements TooltipData {
    private final List<ItemStack> coinStacks;

    public CoinPileTooltipData(List<ItemStack> coinStacks) {
        this.coinStacks = coinStacks;
    }

    public List<ItemStack> getCoinStacks() {
        return this.coinStacks;
    }
}
