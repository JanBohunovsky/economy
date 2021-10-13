package dev.bohush.economy.screen;

import dev.bohush.economy.inventory.ShopStorage;
import dev.bohush.economy.item.CoinPileItem;
import dev.bohush.economy.screen.slot.ShopCoinSlot;
import dev.bohush.economy.screen.slot.ShopStorageSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ShopStorageScreenHandler extends ScreenHandler {
    private final ShopStorage shopStorage;
    private final SimpleInventory coinInventory;

    public ShopStorageScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new ShopStorage(buf.readLong()));
    }

    public ShopStorageScreenHandler(int syncId, PlayerInventory playerInventory, ShopStorage shopStorage) {
        super(ModScreens.SHOP_STORAGE, syncId);
        this.shopStorage = shopStorage;
        this.coinInventory = new SimpleInventory(4);

        shopStorage.onOpen(playerInventory.player);

        // Coin slots
        for (int i = 0; i < coinInventory.size(); i++) {
            this.addSlot(new ShopCoinSlot(this.coinInventory, i, 180, 25 + i * 18));
        }

        // Shop storage
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new ShopStorageSlot(this.shopStorage, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }

        // Player inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 140 + y * 18));
            }
        }

        // Player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 198));
        }

        this.updateCoinInventory();
    }

    public long getCoinValue() {
        return this.shopStorage.getCoins();
    }

    private void updateCoinInventory() {
        var stacks = CoinPileItem.createSplitStacks(this.shopStorage.getCoins());
        this.coinInventory.clear();

        for (int i = 0; i < stacks.size(); i++) {
            this.coinInventory.setStack(i, stacks.get(i));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.shopStorage.canPlayerUse(player);
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.canInsert(stack);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        // Add coins by clicking with items in cursor in shop inventory (storage + coins)
        if (actionType == SlotActionType.PICKUP && indexInShopInventory(slotIndex)) {
            var cursorStack = this.getCursorStack();
            if (CoinPileItem.isCoinPile(cursorStack)) {
                // Left click = all, right click = 1
                var value = CoinPileItem.getValue(cursorStack);
                var amount = button == 0 ? value : CoinPileItem.getHighestCoin(value);
                CoinPileItem.decrementValue(cursorStack, amount);

                if (CoinPileItem.getValue(cursorStack) <= 0) {
                    this.setCursorStack(ItemStack.EMPTY);
                }

                this.shopStorage.addCoins(amount);
                this.updateCoinInventory();
                return;
            }
        }

        // Add coins by swapping items from hotbar into shop inventory
        if (actionType == SlotActionType.SWAP && indexInShopInventory(slotIndex)) {
            var playerInventory = player.getInventory();
            var hotbarStack = playerInventory.getStack(button);

            if (CoinPileItem.isCoinPile(hotbarStack)) {
                playerInventory.setStack(button, ItemStack.EMPTY);
                this.shopStorage.addCoins(hotbarStack);
                this.updateCoinInventory();
                return;
            }
        }

        // Remove coins by throwing items from the coin inventory onto the ground
        if (actionType == SlotActionType.THROW && indexInCoinInventory(slotIndex) && this.getCursorStack().isEmpty()) {
            var slot = this.slots.get(slotIndex);
            var sourceStack = slot.getStack();

            // Default = 1, Ctrl = all
            var value = CoinPileItem.getValue(sourceStack);
            var amount = button == 0 ? CoinPileItem.getHighestCoin(value) : value;

            if (CoinPileItem.getValue(sourceStack) <= 0) {
                slot.setStack(ItemStack.EMPTY);
            }

            this.shopStorage.removeCoins(amount);
            this.updateCoinInventory();

            player.dropItem(CoinPileItem.createStack(amount), true);
            return;
        }

        // Handle QUICK_MOVE in transferSlot
        // Every scenario with `super` has to be below super.onSlotClick call with inverted condition
        //   e.g. cursor has nothing: super + removeCoins => cursor has CoinItem: removeCoins

        super.onSlotClick(slotIndex, button, actionType, player);

        // Remove coins by taking items from coin inventory
        if (actionType == SlotActionType.PICKUP && indexInCoinInventory(slotIndex)) {
            var cursorStack = this.getCursorStack();
            if (CoinPileItem.isCoinPile(cursorStack)) {
                this.shopStorage.removeCoins(cursorStack);
                this.updateCoinInventory();
                return;
            }
        }

        // Remove coins by swapping items from coin inventory
        if (actionType == SlotActionType.SWAP && indexInCoinInventory(slotIndex)) {
            var playerInventory = player.getInventory();
            var hotbarStack = playerInventory.getStack(button);

            if (CoinPileItem.isCoinPile(hotbarStack)) {
                this.shopStorage.removeCoins(hotbarStack);
                this.updateCoinInventory();
            }
        }
    }

    private boolean indexInCoinInventory(int index) {
        return index >= 0 && index < this.coinInventory.size();
    }

    private boolean indexInStorageInventory(int index) {
        return index >= this.coinInventory.size() && index < this.coinInventory.size() + this.shopStorage.size();
    }

    private boolean indexInShopInventory(int index) {
        return indexInCoinInventory(index) || indexInStorageInventory(index);
    }

    private boolean indexInPlayerInventory(int index) {
        return index >= this.coinInventory.size() + this.shopStorage.size() && index < this.slots.size();
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);

        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        // QUICK_MOVE in coinInventory: super + removeCoins
        // QUICK_MOVE in playerInventory:
        //   slot has CoinItem: addCoins

        var stack = slot.getStack();
        var originalStack = stack.copy();

        int storageStart = this.coinInventory.size();
        int playerStart = storageStart + this.shopStorage.size();

        if (indexInCoinInventory(index)) {
            // Insert item from Coin inventory into Player inventory
            if (!this.insertItem(stack, playerStart, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            this.shopStorage.removeCoins(originalStack);
        } else if (indexInStorageInventory(index)) {
            // Insert item from Storage inventory into Player inventory
            if (!this.insertItem(stack, playerStart, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (indexInPlayerInventory(index)) {
            // Insert item from Player inventory into Storage inventory
            if (CoinPileItem.isCoinPile(stack)) {
                this.shopStorage.addCoins(stack);
                CoinPileItem.setValue(stack, 0);
            } else if (!this.insertItem(stack, storageStart, playerStart, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (CoinPileItem.isCoinPile(originalStack)) {
            this.updateCoinInventory();
        }

        return originalStack;
    }
}
