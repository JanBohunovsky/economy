package urfriders.economy.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import urfriders.economy.util.CoinHelper;

public class ShopStorageSlot extends Slot {

    public ShopStorageSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !CoinHelper.isCoinItem(stack);
    }
}