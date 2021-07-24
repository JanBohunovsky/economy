package urfriders.economy.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

public class ShopOffer {
    private final ItemStack firstBuyItem;
    private final ItemStack secondBuyItem;
    private final ItemStack sellItem;
    private int tradesLeft;
    private boolean disabled;
    private boolean fullStorage;

    public ShopOffer(ItemStack buyItem, ItemStack sellItem, int tradesLeft) {
        this(buyItem, ItemStack.EMPTY, sellItem, tradesLeft);
    }

    public ShopOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, ItemStack sellItem, int tradesLeft) {
        this.firstBuyItem = firstBuyItem;
        this.secondBuyItem = secondBuyItem;
        this.sellItem = sellItem;
        this.tradesLeft = tradesLeft;
    }

    public ItemStack getFirstBuyItem() {
        return this.firstBuyItem;
    }

    public ItemStack getSecondBuyItem() {
        return this.secondBuyItem;
    }

    public ItemStack getSellItem() {
        return this.sellItem;
    }

    public ItemStack copySellItem() {
        return this.sellItem.copy();
    }

    public int getTradesLeft() {
        return this.tradesLeft;
    }

    public void onTrade() {
        this.tradesLeft--;
    }

    public boolean isOutOfStock() {
        return this.tradesLeft <= 0;
    }

    public boolean isStorageFull() {
        return this.fullStorage;
    }

    public boolean isManuallyDisabled() {
        return this.disabled;
    }

    public boolean isDisabled() {
        return this.isManuallyDisabled() || this.isOutOfStock() || this.isStorageFull();
    }

    public String getDisabledReason() {
        if (this.isOutOfStock()) {
            return "outOfStock";
        }
        if (this.isStorageFull()) {
            return "fullStorage";
        }

        return "disabled";
    }

    public void setFullStorage(boolean fullStorage) {
        this.fullStorage = fullStorage;
    }

    public void enable() {
        this.disabled = false;
    }

    public void disable() {
        this.disabled = true;
    }

    public boolean update(ShopStorage storage) {
        // Check if each buy item can fit into storage.
        // Check if the storage has sell item.
        // Return if this offer has changed.
        return false;
    }

    public boolean matchesBuyItems(ItemStack firstBuyItem, ItemStack secondBuyItem) {
        return this.acceptsBuyItem(firstBuyItem, this.firstBuyItem) && firstBuyItem.getCount() >= this.firstBuyItem.getCount()
            && this.acceptsBuyItem(secondBuyItem, this.secondBuyItem) && secondBuyItem.getCount() >= this.secondBuyItem.getCount();
    }

    public static ShopOffer fromNbt(NbtCompound nbt) {
        ShopOffer offer = new ShopOffer(
            ItemStack.fromNbt(nbt.getCompound("buy")),
            ItemStack.fromNbt(nbt.getCompound("buyExtra")),
            ItemStack.fromNbt(nbt.getCompound("sell")),
            nbt.getInt("tradesLeft")
        );

        offer.fullStorage = nbt.getBoolean("fullStorage");
        offer.disabled = nbt.getBoolean("disabled");

        return offer;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("buy", firstBuyItem.writeNbt(new NbtCompound()));
        nbt.put("buyExtra", secondBuyItem.writeNbt(new NbtCompound()));
        nbt.put("sell", sellItem.writeNbt(new NbtCompound()));
        nbt.putInt("tradesLeft", tradesLeft);
        nbt.putBoolean("fullStorage", fullStorage);
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
