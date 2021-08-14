package dev.bohush.economy.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

public class CoinItem extends BasicItem {

    private final int tier;

    public CoinItem(int tier) {
        super(new FabricItemSettings().group(ModItemGroup.ALL));
        this.tier = tier;
    }

    public int getTier() {
        return this.tier;
    }

    public long getValue() {
        return (long)Math.pow(this.getMaxCount(), this.getTier());
    }
}
