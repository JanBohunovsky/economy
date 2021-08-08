package urfriders.economy.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.stat.Stats;
import urfriders.economy.inventory.TradeInventory;
import urfriders.economy.shop.Shop;
import urfriders.economy.shop.ShopOffer;

public class ShopOutputSlot extends Slot {
    private final Shop shop;
    private final TradeInventory tradeInventory;
    private int amount;

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
    public ItemStack takeStack(int amount) {
        if (this.hasStack()) {
            this.amount += Math.min(amount, this.getStack().getCount());
        }

        return super.takeStack(amount);
    }

    @Override
    protected void onCrafted(ItemStack stack, int amount) {
        // TODO: This is probably useless so remove it when confirmed
        this.amount += amount;
        this.onCrafted(stack);
    }

    @Override
    protected void onCrafted(ItemStack stack) {
//        stack.onCraft(this.player.world, this.player, this.amount);
        this.amount = 0;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.onCrafted(stack);

        ShopOffer offer = this.tradeInventory.getOffer();
        if (offer == null) {
            return;
        }

        ItemStack firstBuyItem = this.tradeInventory.getStack(0);
        ItemStack secondBuyItem = this.tradeInventory.getStack(1);
        if (offer.depleteBuyItems(firstBuyItem, secondBuyItem) || offer.depleteBuyItems(secondBuyItem, firstBuyItem)) {
            this.shop.trade(offer);
            player.incrementStat(Stats.TRADED_WITH_VILLAGER);

            // Send update to the slots that the stacks have changed
            this.tradeInventory.setStack(0, firstBuyItem);
            this.tradeInventory.setStack(1, secondBuyItem);
        }
    }
}
