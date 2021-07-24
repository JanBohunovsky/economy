package urfriders.economy.shop;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class ShopStorage implements Inventory {
    public static final int SIZE = 36;

    private final DefaultedList<ItemStack> stacks;
    private final CanPlayerUseCheck canPlayerUseCheck;
    private List<InventoryChangedListener> listeners;

    public ShopStorage(CanPlayerUseCheck canPlayerUseCheck) {
        this.stacks = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
        this.canPlayerUseCheck = canPlayerUseCheck;
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
        for (ItemStack stack : this.stacks) {
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
        ItemStack result = Inventories.splitStack(this.stacks, slot, amount);

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

        for (InventoryChangedListener listener : this.listeners) {
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

    /**
     * Adds an ItemStack to an existing slot if possible, otherwise to an empty slot, if possible.
     * @param stack ItemStack to add.
     * @return Leftover ItemStack aka what could not fit into this storage.
     */
    public ItemStack addStack(ItemStack stack) {
        ItemStack sourceStack = stack.copy();
        this.addToExistingSlot(sourceStack);

        if (sourceStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        this.addToNewSlot(sourceStack);
        return sourceStack.isEmpty() ? ItemStack.EMPTY : sourceStack;
    }

    public ItemStack removeStack(ItemStack stack) {
        ItemStack targetStack = stack.copy();

        for (ItemStack storageStack : this.stacks) {
            if (ItemStack.canCombine(storageStack, targetStack)) {
                int amount = Math.min(targetStack.getCount(), storageStack.getCount());
                storageStack.decrement(amount);
                targetStack.decrement(amount);
                this.markDirty();
            }

            if (targetStack.isEmpty()) {
                break;
            }
        }

        if (targetStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            return targetStack;
        }
    }

    private void addToExistingSlot(ItemStack stack) {
        for (ItemStack storageStack : this.stacks) {
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
            ItemStack storageStack = this.stacks.get(i);
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

    public NbtList toNbt() {
        NbtList result = new NbtList();

        for (int i = 0; i < this.stacks.size(); i++) {
            ItemStack stack = this.stacks.get(i);
            if (!stack.isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte)i);
                stack.writeNbt(nbtCompound);
                result.add(nbtCompound);
            }
        }

        return result;
    }

    public void fromNbt(NbtList nbtList) {
        this.stacks.clear();

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            int slot = nbtCompound.getByte("Slot") & 255;
            if (slot < this.stacks.size()) {
                this.stacks.set(slot, ItemStack.fromNbt(nbtCompound));
            }
        }
    }

    public interface CanPlayerUseCheck {
        boolean canPlayerUse(PlayerEntity player);
    }
}
