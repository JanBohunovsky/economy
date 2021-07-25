package urfriders.economy.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class ShopOffer {
    private final ItemStack firstBuyItem;
    private final ItemStack secondBuyItem;
    private final ItemStack sellItem;
    private boolean disabled;
    private int tradesLeft;
    private int availableSpaceForFirstItem;
    private int availableSpaceForSecondItem;

    public ShopOffer(ItemStack buyItem, ItemStack sellItem) {
        this(buyItem, ItemStack.EMPTY, sellItem);
    }

    public ShopOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, ItemStack sellItem) {
        this(firstBuyItem, secondBuyItem, sellItem, false);
    }

    public ShopOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, ItemStack sellItem, boolean disabled) {
        this(firstBuyItem, secondBuyItem, sellItem, disabled, 0, 0, 0);
    }

    private ShopOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, ItemStack sellItem, boolean disabled, int tradesLeft, int availableSpaceForFirstItem, int availableSpaceForSecondItem) {
        this.firstBuyItem = firstBuyItem;
        this.secondBuyItem = secondBuyItem;
        this.sellItem = sellItem;
        this.disabled = disabled;
        this.tradesLeft = tradesLeft;
        this.availableSpaceForFirstItem = availableSpaceForFirstItem;
        this.availableSpaceForSecondItem = availableSpaceForSecondItem;
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

    public boolean isDisabled() {
        return this.disabled || this.tradesLeft <= 0 || this.availableSpaceForFirstItem <= 0 || this.availableSpaceForSecondItem <= 0;
    }

    public String getDisabledReason() {
        if (this.disabled) {
            return "disabled";
        }

        if (this.tradesLeft <= 0) {
            return "outOfStock";
        }

        if (this.availableSpaceForFirstItem <= 0 || this.availableSpaceForSecondItem <= 0) {
            return "fullStorage";
        }

        return "none";
    }

    public void enable() {
        this.disabled = false;
    }

    public void disable() {
        this.disabled = true;
    }

    @Deprecated
    public int getTradesLeft() {
        return this.tradesLeft;
    }

    @Deprecated
    public int getAvailableSpaceForFirstItem() {
        return this.availableSpaceForFirstItem;
    }

    @Deprecated
    public int getAvailableSpaceForSecondItem() {
        return this.availableSpaceForSecondItem;
    }

    public void onTrade() {
        this.tradesLeft--;
        this.availableSpaceForFirstItem -= this.firstBuyItem.getCount();
        this.availableSpaceForSecondItem -= this.secondBuyItem.getCount();
    }

    public boolean update(ShopStorage storage, int emptySlots) {
        int newTradesLeft = (int)Math.floor(storage.getItemCount(this.sellItem) / (double)this.sellItem.getCount());
        int newSpaceForFirst = storage.getExclusiveAvailableSpaceFor(this.firstBuyItem)
            + (emptySlots * Math.min(this.firstBuyItem.getMaxCount(), storage.getMaxCountPerStack()));

        int newSpaceForSecond = 1;
        if (!this.secondBuyItem.isEmpty()) {
            newSpaceForSecond = storage.getExclusiveAvailableSpaceFor(this.secondBuyItem)
                + ((emptySlots - 1) * Math.min(this.secondBuyItem.getMaxCount(), storage.getMaxCountPerStack()));
        }

        boolean changed = this.tradesLeft != newTradesLeft
            || this.availableSpaceForFirstItem != newSpaceForFirst
            || this.availableSpaceForSecondItem != newSpaceForSecond;

        this.tradesLeft = newTradesLeft;
        this.availableSpaceForFirstItem = newSpaceForFirst;
        this.availableSpaceForSecondItem = newSpaceForSecond;

        return changed;
    }

    public boolean matchesBuyItems(ItemStack firstBuyItem, ItemStack secondBuyItem) {
        return this.acceptsBuyItem(firstBuyItem, this.firstBuyItem) && firstBuyItem.getCount() >= this.firstBuyItem.getCount()
            && this.acceptsBuyItem(secondBuyItem, this.secondBuyItem) && secondBuyItem.getCount() >= this.secondBuyItem.getCount();
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeItemStack(this.firstBuyItem);
        buf.writeItemStack(this.sellItem);

        buf.writeBoolean(!this.secondBuyItem.isEmpty());
        if (!this.secondBuyItem.isEmpty()) {
            buf.writeItemStack(this.secondBuyItem);
        }

        buf.writeBoolean(this.disabled);
        buf.writeInt(this.tradesLeft);
        buf.writeInt(this.availableSpaceForFirstItem);
        buf.writeInt(this.availableSpaceForSecondItem);
    }

    public static ShopOffer fromPacket(PacketByteBuf buf) {
        ItemStack firstBuyItem = buf.readItemStack();
        ItemStack sellItem = buf.readItemStack();

        ItemStack secondBuyItem = ItemStack.EMPTY;
        if (buf.readBoolean()) {
            secondBuyItem = buf.readItemStack();
        }

        boolean disabled = buf.readBoolean();
        int tradesLeft = buf.readInt();
        int availableSpaceForFirstItem = buf.readInt();
        int availableSpaceForSecondItem = buf.readInt();

        return new ShopOffer(firstBuyItem, secondBuyItem, sellItem, disabled, tradesLeft, availableSpaceForFirstItem, availableSpaceForSecondItem);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("buy", firstBuyItem.writeNbt(new NbtCompound()));
        nbt.put("buyExtra", secondBuyItem.writeNbt(new NbtCompound()));
        nbt.put("sell", sellItem.writeNbt(new NbtCompound()));
        nbt.putBoolean("disabled", disabled);
        return nbt;
    }

    public static ShopOffer fromNbt(NbtCompound nbt) {
        return new ShopOffer(
            ItemStack.fromNbt(nbt.getCompound("buy")),
            ItemStack.fromNbt(nbt.getCompound("buyExtra")),
            ItemStack.fromNbt(nbt.getCompound("sell")),
            nbt.getBoolean("disabled")
        );
    }

    private boolean acceptsBuyItem(ItemStack givenItem, ItemStack sampleItem) {
//        if (givenItem.isEmpty() && sampleItem.isEmpty()) {
//            return true;
//        }
//
//        ItemStack copyGivenItem = givenItem.copy();
////        if (copyGivenItem.isDamageable()) {
////            copyGivenItem.setDamage(copyGivenItem.getDamage());
////        }
//
//        boolean equalItems = ItemStack.areItemsEqualIgnoreDamage(copyGivenItem, sampleItem);
//
//        if (equalItems && sampleItem.hasNbt()) {
//            return copyGivenItem.hasNbt() && NbtHelper.matches(sampleItem.getNbt(), copyGivenItem.getNbt(), false);
//        }
//
//        return equalItems;
        return ItemStack.canCombine(givenItem, sampleItem);
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
