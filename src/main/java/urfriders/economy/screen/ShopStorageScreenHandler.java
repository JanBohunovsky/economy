package urfriders.economy.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import urfriders.economy.shop.ShopStorage;

public class ShopStorageScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public ShopStorageScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(ShopStorage.SIZE));
    }

    public ShopStorageScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreens.SHOP_STORAGE, syncId);
        checkSize(inventory, ShopStorage.SIZE);
        this.inventory = inventory;

        inventory.onOpen(playerInventory.player);

        // Shop inventory
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(inventory, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }

        // Player inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 104 + y * 18));
            }
        }

        // Player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 162));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);

        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        if (index < this.inventory.size()) {
            if (!this.insertItem(stack, this.inventory.size(), this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.insertItem(stack, 1, this.inventory.size(), false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return stackCopy;
    }
}
