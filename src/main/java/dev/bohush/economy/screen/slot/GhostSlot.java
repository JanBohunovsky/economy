package dev.bohush.economy.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class GhostSlot extends Slot {

    public GhostSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }

    @Override
    public ItemStack getStack() {
        return super.getStack().copy();
    }

    @Override
    public void setStack(ItemStack stack) {
        ItemStack copy = stack.copy();
        super.setStack(copy);
    }
}
