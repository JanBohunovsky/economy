package dev.bohush.economy.screen;

import dev.bohush.economy.inventory.TradeInventory;
import dev.bohush.economy.item.CoinPileItem;
import dev.bohush.economy.item.ItemStackHelper;
import dev.bohush.economy.screen.slot.ShopOutputSlot;
import dev.bohush.economy.shop.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ShopCustomerScreenHandler extends ScreenHandler implements ShopProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Shop shop;
    private final TradeInventory tradeInventory;
    private final Property offerIndex = new Property() {
        @Override
        public int get() {
            return tradeInventory.getOfferIndex();
        }

        @Override
        public void set(int value) {
            tradeInventory.setOfferIndex(value);
        }
    };

    public ShopCustomerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, ClientShop.FromPacket(playerInventory.player, buf));
    }

    public ShopCustomerScreenHandler(int syncId, PlayerInventory playerInventory, Shop shop) {
        super(ModScreens.SHOP_CUSTOMER, syncId);
        this.shop = shop;
        this.tradeInventory = new TradeInventory(shop);

        // Trading slots
        this.addSlot(new Slot(this.tradeInventory, 0, 137, 37));
        this.addSlot(new Slot(this.tradeInventory, 1, 163, 37));
        this.addSlot(new ShopOutputSlot(this.shop, this.tradeInventory, 2, 221, 37));

        // Player inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 109 + x * 18, 84 + y * 18));
            }
        }

        // Player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 109 + i * 18, 142));
        }

        this.addProperty(this.offerIndex);
    }

    @Override
    public Shop getShop() {
        return this.shop;
    }

    public int getOfferIndex() {
        return this.offerIndex.get();
    }

    @Nullable
    public ShopOffer getSelectedOffer() {
        int selectedIndex = getOfferIndex();
        if (selectedIndex >= 0 && selectedIndex < this.getOffers().size()) {
            // Manually selected offer.
            return this.getOffers().get(selectedIndex);
        } else {
            // Offer based on input items.
            return this.tradeInventory.getOffer();
        }
    }

    public ShopOfferList getOffers() {
        return this.shop.getOffers();
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex == TradeInventory.SELL_SLOT) {
            ShopOffer offer = this.getSelectedOffer();
            // Trade begin
            if (offer == null || !this.shop.canTrade(offer)) {
                return;
            }
        }

        super.onSlotClick(slotIndex, button, actionType, player);

        if (slotIndex == TradeInventory.SELL_SLOT) {
            // Trade end
            // including shift-click trading
            this.shop.updateOffers();
        }
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id >= 0 && id < this.shop.getOffers().size()) {
            this.offerIndex.set(id);
            this.switchTo(id);
        }

        return true;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.tradeInventory.updateOffers();
        super.onContentChanged(inventory);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.shop.getActivePlayer() == player;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        // Disables the double click merge stack thing
        return false;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasStack()) {
            var originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (index == 2) {
                // Output slot
                if (!insertItem(originalStack, this.tradeInventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickTransfer(originalStack, newStack);
            } else if (index < 2) {
                // Input slots
                if (!insertItem(originalStack, this.tradeInventory.size(), this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory - move between hotbar and inventory
                if (index < 30) {
                    if (!insertItem(originalStack, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 39 && !insertItem(originalStack, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, originalStack);
        }

        return newStack;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.shop.setActivePlayer(null);
        if (this.shop.getWorld().isClient) {
            return;
        }

        if (!player.isAlive() || player instanceof ServerPlayerEntity serverPlayerEntity && serverPlayerEntity.isDisconnected()) {
            var firstBuyItem = this.tradeInventory.removeStack(0);
            if (!firstBuyItem.isEmpty()) {
                player.dropItem(firstBuyItem, false);
            }

            var secondBuyItem = this.tradeInventory.removeStack(1);
            if (!secondBuyItem.isEmpty()) {
                player.dropItem(secondBuyItem, false);
            }
        } else if (player instanceof ServerPlayerEntity) {
            player.getInventory().offerOrDrop(this.tradeInventory.removeStack(0));
            player.getInventory().offerOrDrop(this.tradeInventory.removeStack(1));
        }
    }

    private void switchTo(int offerIndex) {
        if (offerIndex < getOffers().size()) {
            // Give the player their items back
            var firstBuyItem = this.tradeInventory.getStack(0);
            if (!firstBuyItem.isEmpty()) {
                if (!insertItem(firstBuyItem, 3, 39, true)) {
                    return;
                }

                this.tradeInventory.setStack(0, firstBuyItem);
            }

            var secondBuyItem = this.tradeInventory.getStack(1);
            if (!secondBuyItem.isEmpty()) {
                if (!insertItem(secondBuyItem, 3, 39, true)) {
                    return;
                }

                this.tradeInventory.setStack(1, secondBuyItem);
            }

            // Try to fill in the new items
            if (this.tradeInventory.getStack(0).isEmpty() && this.tradeInventory.getStack(1).isEmpty()) {
                var newFirstBuyItem = getOffers().get(offerIndex).getFirstBuyItem();
                this.autofill(0, newFirstBuyItem);
                var newSecondBuyItem = getOffers().get(offerIndex).getSecondBuyItem();
                this.autofill(1, newSecondBuyItem);
            }
        }
    }


    private void autofill(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        for (int i = 3; i < 39; i++) {
            var inventoryStack = this.slots.get(i).getStack();
            if (inventoryStack.isEmpty()) {
                continue;
            }

            if (ItemStackHelper.isCoinPile(stack, inventoryStack)) {
                var tradeStack = this.tradeInventory.getStack(slot);
                var alreadyInTrade = CoinPileItem.getValue(tradeStack);
                var toBeMoved = CoinPileItem.getValue(inventoryStack);

                CoinPileItem.setValue(inventoryStack, 0);

                var newStack = CoinPileItem.createStack(alreadyInTrade + toBeMoved);
                this.tradeInventory.setStack(slot, newStack);
            } else if (ItemStack.canCombine(stack, inventoryStack)) {
                var tradeStack = this.tradeInventory.getStack(slot);
                int alreadyInTrade = tradeStack.isEmpty() ? 0 : tradeStack.getCount();
                int toBeMoved = Math.min(stack.getMaxCount() - alreadyInTrade, inventoryStack.getCount());

                var newStack = inventoryStack.copy();
                int newCount = alreadyInTrade + toBeMoved;

                inventoryStack.decrement(toBeMoved);
                newStack.setCount(newCount);
                this.tradeInventory.setStack(slot, newStack);

                if (newCount >= stack.getMaxCount()) {
                    break;
                }
            }
        }
    }
}
