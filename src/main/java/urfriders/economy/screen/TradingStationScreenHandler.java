package urfriders.economy.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import urfriders.economy.block.entity.TradingStationBlockEntity;

public class TradingStationScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public TradingStationScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(2));
    }

    public TradingStationScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreens.TRADING_STATION, syncId);
        checkSize(inventory, 2);
        this.inventory = inventory;

        inventory.onOpen(playerInventory.player);

        // Trading Station inventory
        this.addSlot(new Slot(inventory, 0, 62, 35));
        this.addSlot(new Slot(inventory, 1, 98, 35));

        // Player inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        // Player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public void spawnVillager(ServerWorld world) {
        if (inventory instanceof TradingStationBlockEntity tradingStationBlockEntity) {
            System.out.println("TradingStationScreenHandler: inventory is block entity");
            tradingStationBlockEntity.spawnVillager(world);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (index < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }
}
