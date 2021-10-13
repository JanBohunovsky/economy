package dev.bohush.economy.screen.slot;

import dev.bohush.economy.inventory.TradeInventory;
import dev.bohush.economy.shop.Shop;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.stat.Stats;

public class ShopOutputSlot extends Slot {
    private final Shop shop;
    private final TradeInventory tradeInventory;

    public ShopOutputSlot(Shop shop, TradeInventory tradeInventory, int index, int x, int y) {
        super(tradeInventory, index, x, y);
        this.shop = shop;
        this.tradeInventory = tradeInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        var offer = this.tradeInventory.getOffer();
        if (offer == null) {
            return;
        }

        var firstBuyItem = this.tradeInventory.getStack(0);
        var secondBuyItem = this.tradeInventory.getStack(1);
        if (offer.depleteBuyItems(firstBuyItem, secondBuyItem) || offer.depleteBuyItems(secondBuyItem, firstBuyItem)) {
            this.shop.trade(offer);
            player.incrementStat(Stats.TRADED_WITH_VILLAGER);

            // Send update to the slots that the stacks have changed
            this.tradeInventory.setStack(0, firstBuyItem);
            this.tradeInventory.setStack(1, secondBuyItem);
        }
    }
}
