package dev.bohush.economy.inventory;

import dev.bohush.economy.shop.Shop;
import dev.bohush.economy.shop.ShopOffer;
import dev.bohush.economy.shop.ShopOfferList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class TradeInventory implements Inventory {
    public static final int SELL_SLOT = 2;

    private final Shop shop;
    private final DefaultedList<ItemStack> inventory;
    private int offerIndex = -1;

    @Nullable
    private ShopOffer currentOffer;

    public TradeInventory(Shop shop) {
        this.shop = shop;
        this.inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : this.inventory) {
            if (!item.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = this.inventory.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (slot == SELL_SLOT) {
            return Inventories.splitStack(this.inventory, slot, stack.getCount());
        }

        ItemStack remainder = Inventories.splitStack(this.inventory, slot, amount);
        if (!remainder.isEmpty()) {
            this.updateOffers();
        }

        return remainder;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }

        if (slot < SELL_SLOT) {
            this.updateOffers();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return shop.getActivePlayer() == player;
    }

    @Override
    public void markDirty() {
        this.updateOffers();
    }

    public void updateOffers() {
        this.currentOffer = null;
        ItemStack firstBuyItem;
        ItemStack secondBuyItem;

        if (this.inventory.get(0).isEmpty()) {
            firstBuyItem = this.inventory.get(1);
            secondBuyItem = ItemStack.EMPTY;
        } else {
            firstBuyItem = this.inventory.get(0);
            secondBuyItem = this.inventory.get(1);
        }

        if (firstBuyItem.isEmpty()) {
            this.setStack(SELL_SLOT, ItemStack.EMPTY);
            return;
        }

        ShopOfferList offers = shop.getOffers();
        if (offers.isEmpty()) {
            return;
        }

        ShopOffer offer = offers.getValidOffer(firstBuyItem, secondBuyItem, this.offerIndex);
        if (offer == null || offer.isDisabled()) {
            this.currentOffer = offer;
            offer = offers.getValidOffer(secondBuyItem, firstBuyItem, this.offerIndex);
        }

        if (offer != null && !offer.isDisabled()) {
            this.currentOffer = offer;
            this.setStack(SELL_SLOT, offer.getSellItem().copy());
        } else {
            this.setStack(SELL_SLOT, ItemStack.EMPTY);
        }

        this.shop.onSellingItem(this.getStack(SELL_SLOT));
    }

    @Nullable
    public ShopOffer getOffer() {
        return this.currentOffer;
    }

    public int getOfferIndex() {
        return this.offerIndex;
    }

    public void setOfferIndex(int index) {
        this.offerIndex = index;
        this.updateOffers();
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }
}
