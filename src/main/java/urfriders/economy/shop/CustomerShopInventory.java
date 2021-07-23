package urfriders.economy.shop;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CustomerShopInventory extends ShopInventory {

    @Nullable
    private ShopOffer currentOffer;

    public CustomerShopInventory(Shop shop) {
        super(shop);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = inventory.get(slot);
        if (itemStack.isEmpty()) {
            return itemStack;
        }

        if (slot == 2) {
            return Inventories.splitStack(inventory, slot, itemStack.getCount());
        }

        itemStack = Inventories.splitStack(inventory, slot, amount);
        markDirty();

        return itemStack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }

        if (slot == 0 || slot == 1) {
            markDirty();
        }
    }

    private boolean needsOfferUpdate(int slot) {
        return slot == 0 || slot == 1;
    }

    @Override
    public void markDirty() {
        updateOffers();
    }

    private void updateOffers() {
        currentOffer = null;
        ItemStack firstBuyItem;
        ItemStack secondBuyItem;

        if (inventory.get(0).isEmpty()) {
            firstBuyItem = inventory.get(1);
            secondBuyItem = ItemStack.EMPTY;
        } else {
            firstBuyItem = inventory.get(0);
            secondBuyItem = inventory.get(1);
        }

        if (firstBuyItem.isEmpty()) {
            setStack(2, ItemStack.EMPTY);
            return;
        }

        ShopOfferList offers = shop.getOffers();
        if (offers.isEmpty()) {
            return;
        }

        ShopOffer offer = offers.getValidOffer(firstBuyItem, secondBuyItem, offerIndex);
        if (offer == null || offer.isDisabled()) {
            currentOffer = offer;
            offer = offers.getValidOffer(secondBuyItem, firstBuyItem, offerIndex);
        }

        if (offer != null && !offer.isDisabled()) {
            currentOffer = offer;
            setStack(2, offer.copySellItem());
        } else {
            setStack(2, ItemStack.EMPTY);
        }

        shop.onSellingItem(getStack(2));
    }

    @Nullable
    public ShopOffer getCurrentOffer() {
        return currentOffer;
    }
}
