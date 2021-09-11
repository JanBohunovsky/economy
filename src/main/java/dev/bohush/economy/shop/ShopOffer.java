package dev.bohush.economy.shop;

import dev.bohush.economy.inventory.ShopStorage;
import dev.bohush.economy.util.CoinHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ShopOffer {
    private final ItemStack firstBuyItem;
    private final ItemStack secondBuyItem;
    private final ItemStack sellItem;
    private boolean locked;
    private int sellItemStock;
    private int availableSpaceForFirstItem;
    private int availableSpaceForSecondItem;

    public ShopOffer(ItemStack buyItem, ItemStack sellItem) {
        this(buyItem, ItemStack.EMPTY, sellItem);
    }

    public ShopOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, ItemStack sellItem) {
        this(firstBuyItem, secondBuyItem, sellItem, false);
    }

    public ShopOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, ItemStack sellItem, boolean locked) {
        this(firstBuyItem, secondBuyItem, sellItem, locked, 0, 0, 0);
    }

    private ShopOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, ItemStack sellItem, boolean locked, int sellItemStock, int availableSpaceForFirstItem, int availableSpaceForSecondItem) {
        this.firstBuyItem = firstBuyItem;
        this.secondBuyItem = secondBuyItem;
        this.sellItem = sellItem;
        this.locked = locked;
        this.sellItemStock = sellItemStock;
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

    public boolean isDisabled() {
        return this.locked || this.isOutOfStock() || this.isStorageFull();
    }

    public Text getDisabledReasonText() {
        if (this.locked) {
            return new TranslatableText("shop.offer.locked");
        }

        if (this.isOutOfStock()) {
            return CoinHelper.isCoinItem(this.sellItem)
                ? new TranslatableText("shop.offer.outOfCoins")
                : new TranslatableText("shop.offer.outOfStock");
        }

        if (this.isStorageFull()) {
            return new TranslatableText("shop.offer.fullStorage");
        }

        return new LiteralText("");
    }

    private boolean isOutOfStock() {
        return this.sellItemStock < this.sellItem.getCount();
    }

    private boolean isStorageFull() {
        return this.availableSpaceForFirstItem < this.firstBuyItem.getCount()
            || this.availableSpaceForSecondItem < this.secondBuyItem.getCount();
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void unlock() {
        this.locked = false;
    }

    public void lock() {
        this.locked = true;
    }

    @Deprecated
    public int getSellItemStock() {
        return this.sellItemStock;
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
        this.sellItemStock -= this.sellItem.getCount();
        this.availableSpaceForFirstItem -= this.firstBuyItem.getCount();
        this.availableSpaceForSecondItem -= this.secondBuyItem.getCount();
    }

    public boolean update(ShopStorage storage, int emptyStorageSlots) {
        int newTradesLeft = storage.getItemCount(this.sellItem);
        int newSpaceForFirst = this.calculateAvailableSpaceFor(this.firstBuyItem, storage, emptyStorageSlots);
        int newSpaceForSecond = this.calculateAvailableSpaceFor(this.secondBuyItem, storage, emptyStorageSlots - 1);

        boolean changed = this.sellItemStock != newTradesLeft
            || this.availableSpaceForFirstItem != newSpaceForFirst
            || this.availableSpaceForSecondItem != newSpaceForSecond;

        this.sellItemStock = newTradesLeft;
        this.availableSpaceForFirstItem = newSpaceForFirst;
        this.availableSpaceForSecondItem = newSpaceForSecond;

        return changed;
    }

    private int calculateAvailableSpaceFor(ItemStack itemStack, ShopStorage storage, int emptyStorageSlots) {
        if (itemStack.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        if (CoinHelper.isCoinItem(itemStack)) {
            return Integer.MAX_VALUE;
        }

        int exclusiveSpace = storage.getExclusiveAvailableSpaceFor(itemStack);
        int maxStackCount = Math.min(itemStack.getMaxCount(), storage.getMaxCountPerStack());
        int emptySpace = Math.max(emptyStorageSlots, 0) * maxStackCount;

        return exclusiveSpace + emptySpace;
    }

    public boolean matchesBuyItems(ItemStack firstBuyItem, ItemStack secondBuyItem) {
        return ItemStack.canCombine(firstBuyItem, this.firstBuyItem) && firstBuyItem.getCount() >= this.firstBuyItem.getCount()
            && ItemStack.canCombine(secondBuyItem, this.secondBuyItem) && secondBuyItem.getCount() >= this.secondBuyItem.getCount();
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeItemStack(this.firstBuyItem);
        buf.writeItemStack(this.sellItem);

        buf.writeBoolean(!this.secondBuyItem.isEmpty());
        if (!this.secondBuyItem.isEmpty()) {
            buf.writeItemStack(this.secondBuyItem);
        }

        buf.writeBoolean(this.locked);
        buf.writeInt(this.sellItemStock);
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

        boolean locked = buf.readBoolean();
        int sellItemStock = buf.readInt();
        int availableSpaceForFirstItem = buf.readInt();
        int availableSpaceForSecondItem = buf.readInt();

        return new ShopOffer(firstBuyItem, secondBuyItem, sellItem, locked, sellItemStock, availableSpaceForFirstItem, availableSpaceForSecondItem);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("buy", firstBuyItem.writeNbt(new NbtCompound()));
        nbt.put("buyExtra", secondBuyItem.writeNbt(new NbtCompound()));
        nbt.put("sell", sellItem.writeNbt(new NbtCompound()));
        nbt.putBoolean("locked", locked);
        return nbt;
    }

    public static ShopOffer fromNbt(NbtCompound nbt) {
        boolean locked = false;
        if (nbt.contains("locked", NbtElement.BYTE_TYPE)) {
            locked = nbt.getBoolean("locked");
        } else if (nbt.contains("disabled", NbtElement.BYTE_TYPE)) {
            locked = nbt.getBoolean("disabled");
        }

        return new ShopOffer(
            ItemStack.fromNbt(nbt.getCompound("buy")),
            ItemStack.fromNbt(nbt.getCompound("buyExtra")),
            ItemStack.fromNbt(nbt.getCompound("sell")),
            locked //nbt.getBoolean("locked")
        );
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
