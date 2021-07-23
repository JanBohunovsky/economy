package urfriders.economy.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

public class ShopOffer {
    private final ItemStack firstBuyItem;
    private final ItemStack secondBuyItem;
    private final ItemStack sellItem;
    private int stock;
    private boolean disabled;

    public ShopOffer(ItemStack buyItem, ItemStack sellItem, int stock) {
        this(buyItem, ItemStack.EMPTY, sellItem, stock);
    }

    public ShopOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, ItemStack sellItem, int stock) {
        this.firstBuyItem = firstBuyItem;
        this.secondBuyItem = secondBuyItem;
        this.sellItem = sellItem;
        this.stock = stock;
    }

    public ItemStack getFirstBuyItem() {
        return firstBuyItem;
    }

    public ItemStack getSecondBuyItem() {
        return secondBuyItem;
    }

    public ItemStack getSellItem() {
        return sellItem;
    }

    public ItemStack copySellItem() {
        return sellItem.copy();
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void decrementStock() {
        stock--;
    }

    public boolean isOutOfStock() {
        return stock <= 0;
    }

    public boolean isManuallyDisabled() {
        return disabled;
    }

    public boolean isDisabled() {
        return isOutOfStock() || isManuallyDisabled();
    }

    public void enable() {
        disabled = false;
    }

    public void disable() {
        disabled = true;
    }

    public boolean matchesBuyItems(ItemStack firstBuyItem, ItemStack secondBuyItem) {
        return acceptsBuyItem(firstBuyItem, this.firstBuyItem) && firstBuyItem.getCount() >= this.firstBuyItem.getCount()
            && acceptsBuyItem(secondBuyItem, this.secondBuyItem) && secondBuyItem.getCount() >= this.secondBuyItem.getCount();
    }

    public static ShopOffer fromNbt(NbtCompound nbt) {
        ShopOffer offer = new ShopOffer(
            ItemStack.fromNbt(nbt.getCompound("buy")),
            ItemStack.fromNbt(nbt.getCompound("buyExtra")),
            ItemStack.fromNbt(nbt.getCompound("sell")),
            nbt.getInt("stock")
        );

        offer.disabled = nbt.getBoolean("disabled");

        return offer;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("buy", firstBuyItem.writeNbt(new NbtCompound()));
        nbt.put("buyExtra", secondBuyItem.writeNbt(new NbtCompound()));
        nbt.put("sell", sellItem.writeNbt(new NbtCompound()));
        nbt.putInt("stock", stock);
        nbt.putBoolean("disabled", disabled);
        return nbt;
    }

    private boolean acceptsBuyItem(ItemStack givenItem, ItemStack sampleItem) {
        if (givenItem.isEmpty() && sampleItem.isEmpty()) {
            return true;
        }

        ItemStack copyGivenItem = givenItem.copy();
        if (copyGivenItem.isDamageable()) {
            copyGivenItem.setDamage(copyGivenItem.getDamage());
        }

        boolean equalBase = ItemStack.areItemsEqualIgnoreDamage(copyGivenItem, sampleItem);

        if (equalBase && sampleItem.hasNbt()) {
            return copyGivenItem.hasNbt() && NbtHelper.matches(sampleItem.getNbt(), copyGivenItem.getNbt(), false);
        }

        return equalBase;
    }

    public boolean depleteBuyItems(ItemStack firstBuyItem, ItemStack secondBuyItem) {
        if (!this.matchesBuyItems(firstBuyItem, secondBuyItem)) {
            return false;
        }

        firstBuyItem.decrement(this.firstBuyItem.getCount());
        if (!this.secondBuyItem.isEmpty()) {
            secondBuyItem.decrement(this.secondBuyItem.getCount());
        }

        return true;
    }
}
