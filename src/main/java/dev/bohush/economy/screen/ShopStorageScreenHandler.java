package dev.bohush.economy.screen;

import dev.bohush.economy.inventory.ShopStorage;
import dev.bohush.economy.item.CoinItem;
import dev.bohush.economy.screen.slot.ShopCoinSlot;
import dev.bohush.economy.screen.slot.ShopStorageSlot;
import dev.bohush.economy.util.CoinHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class ShopStorageScreenHandler extends ScreenHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ShopStorage shopStorage;
    private final SimpleInventory coinInventory;

    public ShopStorageScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new ShopStorage(buf.readLong()));
    }

    public ShopStorageScreenHandler(int syncId, PlayerInventory playerInventory, ShopStorage shopStorage) {
        super(ModScreens.SHOP_STORAGE, syncId);
        this.shopStorage = shopStorage;
        this.coinInventory = new SimpleInventory(CoinHelper.getCoinCount());

        shopStorage.onOpen(playerInventory.player);

        // Coin slots
        for (int i = 0; i < coinInventory.size(); i++) {
            this.addSlot(new ShopCoinSlot(this.coinInventory, i, 180, 97 - i * 18));
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
        ArrayList<ItemStack> coinStacks = CoinHelper.getItemStacks(this.shopStorage.getCoins());

        this.coinInventory.clear();

        for (int i = coinStacks.size() - 1; i >= 0; i--) {
            ItemStack coinStack = coinStacks.get(i);
            CoinItem coinItem = (CoinItem)coinStack.getItem();

            this.coinInventory.setStack(coinItem.getTier(), coinStack);
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
        LOGGER.info("onSlotClick(slotIndex={}, button={}, actionType={}, player)", slotIndex, button, actionType.toString());

        // Add coins by clicking with items in cursor in shop inventory (storage + coins)
        if (actionType == SlotActionType.PICKUP && indexInShopInventory(slotIndex)) {
            ItemStack cursorStack = this.getCursorStack();
            if (!cursorStack.isEmpty() && CoinHelper.isCoinItem(cursorStack)) {
                // Left click = all, right click = 1
                int amount = button == 0 ? cursorStack.getCount() : 1;
                ItemStack stackToAdd = cursorStack.split(amount);

                if (cursorStack.isEmpty()) {
                    this.setCursorStack(ItemStack.EMPTY);
                }

                this.shopStorage.addCoins(stackToAdd);
                this.updateCoinInventory();
                return;
            }
        }

        // Add coins by swapping items from hotbar into shop inventory
        if (actionType == SlotActionType.SWAP && indexInShopInventory(slotIndex)) {
            PlayerInventory playerInventory = player.getInventory();
            ItemStack hotbarStack = playerInventory.getStack(button);

            if (!hotbarStack.isEmpty() && CoinHelper.isCoinItem(hotbarStack)) {
                playerInventory.setStack(button, ItemStack.EMPTY);
                this.shopStorage.addCoins(hotbarStack);
                this.updateCoinInventory();
                return;
            }
        }

        // Remove coins by throwing items from the coin inventory onto the ground
        if (actionType == SlotActionType.THROW && indexInCoinInventory(slotIndex) && this.getCursorStack().isEmpty()) {
            Slot slot = this.slots.get(slotIndex);
            ItemStack sourceStack = slot.getStack();

            // Default = 1, Ctrl = all
            int amount = button == 0 ? 1 : sourceStack.getCount();
            ItemStack stackToDrop = sourceStack.split(amount);

            if (sourceStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            }

            this.shopStorage.removeCoins(stackToDrop);
            this.updateCoinInventory();

            player.dropItem(stackToDrop, true);
            return;
        }

        // Handle QUICK_MOVE in transferSlot
        // Every scenario with `super` has to be below super.onSlotClick call with inverted condition
        //   e.g. cursor has nothing: super + removeCoins => cursor has CoinItem: removeCoins

        LOGGER.info("super.onSlotClick(...)");
        super.onSlotClick(slotIndex, button, actionType, player);

        // Remove coins by taking items from coin inventory
        if (actionType == SlotActionType.PICKUP && indexInCoinInventory(slotIndex)) {
            ItemStack cursorStack = this.getCursorStack();
            if (!cursorStack.isEmpty() && CoinHelper.isCoinItem(cursorStack)) {
                this.shopStorage.removeCoins(cursorStack);
                this.updateCoinInventory();
                return;
            }
        }

        // Remove coins by swapping items from coin inventory
        if (actionType == SlotActionType.SWAP && indexInCoinInventory(slotIndex)) {
            PlayerInventory playerInventory = player.getInventory();
            ItemStack hotbarStack = playerInventory.getStack(button);

            if (!hotbarStack.isEmpty() && CoinHelper.isCoinItem(hotbarStack)) {
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
        LOGGER.info("transferSlot(player, index={})", index);
        Slot slot = this.slots.get(index);

        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        // QUICK_MOVE in coinInventory: super + removeCoins
        // QUICK_MOVE in playerInventory:
        //   slot has CoinItem: addCoins

        ItemStack stack = slot.getStack();
        ItemStack originalStack = stack.copy();

        int storageStart = this.coinInventory.size();
        int playerStart = storageStart + this.shopStorage.size();

        if (indexInCoinInventory(index)) {
            // Insert item from Coin inventory into Player inventory
            if (!this.insertItem(stack, playerStart, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            // Remove coins from storage (`stack` is now a remainder from the insertItem method call, so we have to calculate how many items we've moved)
            ItemStack stackToRemove = originalStack.copy();
            stackToRemove.decrement(stack.getCount());
            this.shopStorage.removeCoins(stackToRemove);
        } else if (indexInStorageInventory(index)) {
            // Insert item from Storage inventory into Player inventory
            if (!this.insertItem(stack, playerStart, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (indexInPlayerInventory(index)) {
            // Insert item from Player inventory into Storage inventory
            if (CoinHelper.isCoinItem(stack)) {
                this.shopStorage.addCoins(stack);
                stack.setCount(0);
            } else if (!this.insertItem(stack, storageStart, playerStart, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (CoinHelper.isCoinItem(originalStack)) {
            this.updateCoinInventory();
        }

        return originalStack;
    }
}
