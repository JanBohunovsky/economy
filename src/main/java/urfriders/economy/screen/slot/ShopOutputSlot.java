package urfriders.economy.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.stat.Stats;
import urfriders.economy.shop.CustomerShopInventory;
import urfriders.economy.shop.Shop;
import urfriders.economy.shop.ShopOffer;

public class ShopOutputSlot extends Slot {
    private final Shop shop;
    private final CustomerShopInventory shopInventory;
    private int amount;

    public ShopOutputSlot(Shop shop, CustomerShopInventory shopInventory, int index, int x, int y) {
        super(shopInventory, index, x, y);
        this.shop = shop;
        this.shopInventory = shopInventory;
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

        ShopOffer offer = shopInventory.getCurrentOffer();
        if (offer == null) {
            return;
        }

        ItemStack firstBuyItem = shopInventory.getStack(0);
        ItemStack secondBuyItem = shopInventory.getStack(1);
        if (offer.depleteBuyItems(firstBuyItem, secondBuyItem) || offer.depleteBuyItems(secondBuyItem, firstBuyItem)) {
            shop.trade(offer);
            player.incrementStat(Stats.TRADED_WITH_VILLAGER);
            shopInventory.setStack(0, firstBuyItem);
            shopInventory.setStack(1, secondBuyItem);
        }
    }
}
