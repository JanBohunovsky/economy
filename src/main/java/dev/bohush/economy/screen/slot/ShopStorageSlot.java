package dev.bohush.economy.screen.slot;

import dev.bohush.economy.util.CoinHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ShopStorageSlot extends Slot {

    public ShopStorageSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !CoinHelper.isCoinItem(stack);
    }
}
