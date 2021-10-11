package dev.bohush.economy.inventory;

import com.google.common.collect.Lists;
import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class ShopStorage implements Inventory {
    public static final int SIZE = 54;

    private final DefaultedList<ItemStack> stacks;
    private final CanPlayerUseCheck canPlayerUseCheck;
    private long coins;
    private List<InventoryChangedListener> listeners;

    /**
     * Server-side constructor
     */
    public ShopStorage(CanPlayerUseCheck canPlayerUseCheck) {
        this.stacks = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
        this.canPlayerUseCheck = canPlayerUseCheck;
    }

    /**
     * Client-side constructor
     */
    public ShopStorage(long coins) {
        this.stacks = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
        this.canPlayerUseCheck = player -> true;
        this.coins = coins;
    }

    public void addListener(InventoryChangedListener listener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }

        this.listeners.add(listener);
    }

    public void removeListener(InventoryChangedListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public int size() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (var stack : this.stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= 0 && slot < this.stacks.size()) {
            return this.stacks.get(slot);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        var result = Inventories.splitStack(this.stacks, slot, amount);

        if (!result.isEmpty()) {
            this.markDirty();
        }

        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.stacks, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }

        this.markDirty();
    }

    @Override
    public void markDirty() {
        if (this.listeners == null) {
            return;
        }

        for (var listener : this.listeners) {
            listener.onInventoryChanged(this);
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.canPlayerUseCheck.canPlayerUse(player);
    }

    @Override
    public void clear() {
        this.stacks.clear();
        this.markDirty();
    }

    public long getCoins() {
        return this.coins;
    }

    public void addCoins(long value) {
        this.coins += value;
        this.markDirty();
    }

    public void addCoins(ItemStack stack) {
        this.addCoins(CoinPileItem.getValue(stack));
    }

    public boolean removeCoins(long value) {
        if (this.coins < value) {
            return false;
        }

        this.coins -= value;

        this.markDirty();
        return true;
    }

    public boolean removeCoins(ItemStack stack) {
        return this.removeCoins(CoinPileItem.getValue(stack));
    }

    /**
     * Adds an ItemStack to an existing slot if possible, otherwise to an empty slot, if possible.
     * @param stack ItemStack to add.
     * @return Leftover ItemStack aka what could not fit into this storage.
     */
    public ItemStack addStack(ItemStack stack) {
        if (CoinPileItem.isCoinPile(stack)) {
            this.addCoins(stack);
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = stack.copy();
        this.addToExistingSlot(sourceStack);

        if (sourceStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        this.addToNewSlot(sourceStack);
        return sourceStack.isEmpty() ? ItemStack.EMPTY : sourceStack;
    }

    /**
     * Removes an ItemStack from this storage, wherever it is and may remove only a portion if the storage does not have enough.
     * @param stack ItemStack to remove (including the amount)
     * @return The remaining ItemStack that could not be removed (if any).
     */
    public ItemStack removeStack(ItemStack stack) {
        if (CoinPileItem.isCoinPile(stack)) {
            return this.removeCoins(stack)
                ? ItemStack.EMPTY
                : stack.copy();
        }

        var targetStack = stack.copy();
        for (var storageStack : this.stacks) {
            if (ItemStack.canCombine(storageStack, targetStack)) {
                int amount = Math.min(targetStack.getCount(), storageStack.getCount());
                storageStack.decrement(amount);
                targetStack.decrement(amount);
                this.markDirty();
            }

            if (targetStack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return targetStack;
    }

    public boolean canFit(ItemStack... stacks) {
        int emptySlots = this.getEmptySlotCount();
        int requiredSlots = 0;

        if (CoinPileItem.isCoinPile(stacks)) {
            return true;
        }

        for (var target : stacks) {
            requiredSlots += Math.ceil(target.getCount() / (double)this.getMaxCountPerStack());
        }

        if (requiredSlots <= emptySlots) {
            return true;
        }

        for (var target : stacks) {
            if (this.getExclusiveAvailableSpaceFor(target) < target.getCount()) {
                return false;
            }
        }

        return true;
    }

    public boolean hasStack(ItemStack target) {
        if (target.isEmpty()) {
            return false;
        }

        if (CoinPileItem.isCoinPile(target)) {
            return this.coins >= CoinPileItem.getValue(target);
        }

        int count = 0;
        for (var storageStack : this.stacks) {
            if (ItemStack.canCombine(target, storageStack)) {
                count += storageStack.getCount();
            }

            if (count >= target.getCount()) {
                return true;
            }
        }

        return false;
    }

    public int getEmptySlotCount() {
        int count = 0;
        for (var stack : this.stacks) {
            if (stack.isEmpty()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns number of items that can fit into occupied slots of the same item.
     */
    public int getExclusiveAvailableSpaceFor(ItemStack target) {
        int maxCountPerStack = Math.min(this.getMaxCountPerStack(), target.getMaxCount());
        int count = 0;
        for (var storageStack : this.stacks) {
            if (ItemStack.canCombine(target, storageStack)) {
                count += maxCountPerStack - storageStack.getCount();
            }
        }

        return count;
    }

    public int getItemCount(ItemStack target) {
        if (CoinPileItem.isCoinPile(target)) {
            long count = this.coins / CoinPileItem.getValue(target);

            return count > Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : (int)count;
        }

        int count = 0;
        for (var storageStack : this.stacks) {
            if (ItemStack.canCombine(target, storageStack)) {
                count += storageStack.getCount();
            }
        }

        return count;
    }

    private void addToExistingSlot(ItemStack stack) {
        for (var storageStack : this.stacks) {
            if (ItemStack.canCombine(storageStack, stack)) {
                this.transfer(stack, storageStack);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void addToNewSlot(ItemStack stack) {
        for (int i = 0; i < this.stacks.size(); i++) {
            var storageStack = this.stacks.get(i);
            if (storageStack.isEmpty()) {
                this.setStack(i, stack.copy());
                stack.setCount(0);
                return;
            }
        }
    }

    private void transfer(ItemStack source, ItemStack target) {
        int maxCount = Math.min(this.getMaxCountPerStack(), target.getMaxCount());
        int amount = Math.min(maxCount - target.getCount(), source.getCount());

        if (amount > 0) {
            target.increment(amount);
            source.decrement(amount);
            this.markDirty();
        }
    }

    public NbtCompound toNbt() {
        var nbt = new NbtCompound();
        nbt.putLong(CoinPileItem.COINS_KEY, this.coins);

        var nbtList = new NbtList();
        for (int i = 0; i < this.stacks.size(); i++) {
            var stack = this.stacks.get(i);
            if (!stack.isEmpty()) {
                var nbtItem = new NbtCompound();
                nbtItem.putByte("Slot", (byte)i);
                stack.writeNbt(nbtItem);
                nbtList.add(nbtItem);
            }
        }

        nbt.put("Items", nbtList);

        return nbt;
    }

    public void fromNbt(NbtCompound nbt) {
        this.stacks.clear();
        this.coins = nbt.getLong(CoinPileItem.COINS_KEY);

        var nbtList = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); i++) {
            var nbtItem = nbtList.getCompound(i);
            int slot = nbtItem.getByte("Slot") & 255;
            if (slot < this.stacks.size()) {
                this.stacks.set(slot, ItemStack.fromNbt(nbtItem));
            }
        }
    }

    public interface CanPlayerUseCheck {
        boolean canPlayerUse(PlayerEntity player);
    }
}
