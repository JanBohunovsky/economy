package dev.bohush.economy.screen;

import dev.bohush.economy.screen.slot.GhostSlot;
import dev.bohush.economy.shop.ClientShop;
import dev.bohush.economy.shop.Shop;
import dev.bohush.economy.shop.ShopOffer;
import dev.bohush.economy.shop.ShopProvider;
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
import org.jetbrains.annotations.Nullable;

public class ShopOwnerScreenHandler extends ScreenHandler implements ShopProvider {
    public static final int NEW_OFFER_BUTTON = -10;
    public static final int DELETE_OFFER_BUTTON = -11;
    public static final int MOVE_OFFER_UP_BUTTON = -12;
    public static final int MOVE_OFFER_DOWN_BUTTON = -13;
    public static final int SAVE_UNLOCKED_OFFER_BUTTON = -14;
    public static final int SAVE_LOCKED_OFFER_BUTTON = -15;

    private static final Logger LOGGER = LogManager.getLogger();

    public final Shop shop;

    private final SimpleInventory offerInventory;
    private int offerIndex = -1;

    public ShopOwnerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, ClientShop.FromPacket(playerInventory.player, buf));
    }

    public ShopOwnerScreenHandler(int syncId, PlayerInventory playerInventory, Shop shop) {
        super(ModScreens.SHOP_OWNER, syncId);
        this.shop = shop;
        this.offerInventory = new SimpleInventory(3);

        final int paddingTop = 17;
        // Offer slots
        this.addSlot(new GhostSlot(this.offerInventory, 0, 137, 37 + paddingTop));
        this.addSlot(new GhostSlot(this.offerInventory, 1, 163, 37 + paddingTop));
        this.addSlot(new GhostSlot(this.offerInventory, 2, 221, 37 + paddingTop));

        // Player inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 109 + x * 18, 84 + y * 18 + paddingTop));
            }
        }

        // Player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 109 + i * 18, 142 + paddingTop));
        }
    }

    @Override
    public Shop getShop() {
        return this.shop;
    }

    public int getOfferIndex() {
        return this.offerIndex;
    }

    @Nullable
    public ShopOffer getSelectedOffer() {
        var index = this.getOfferIndex();
        if (index >= 0 && index < this.shop.getOffers().size()) {
            return this.shop.getOffers().get(index);
        }

        return null;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id >= 0 && id < this.shop.getOffers().size()) {
            this.offerIndex = id;
            this.updateOfferSlots();
            return true;
        }

        if (id == NEW_OFFER_BUTTON) {
            var offer = new ShopOffer(ItemStack.EMPTY, ItemStack.EMPTY);
            this.shop.getOffers().add(offer);
            this.shop.markDirty();

            this.offerIndex = this.shop.getOffers().size() - 1;
            updateOfferSlots();
            return true;
        }

        if (this.offerIndex >= 0 && this.offerIndex < this.shop.getOffers().size()) {
            if (id == DELETE_OFFER_BUTTON) {
                this.shop.getOffers().remove(this.offerIndex);
                this.shop.markDirty();

                this.offerIndex = -1;
                updateOfferSlots();
                return true;
            }

            if (id == MOVE_OFFER_UP_BUTTON && this.offerIndex > 0) {
                this.swapOffers(this.offerIndex, -1);
                this.offerIndex--;
                return true;
            }

            if (id == MOVE_OFFER_DOWN_BUTTON && this.offerIndex < this.shop.getOffers().size() - 1) {
                this.swapOffers(this.offerIndex, +1);
                this.offerIndex++;
                return true;
            }

            if (id == SAVE_LOCKED_OFFER_BUTTON || id == SAVE_UNLOCKED_OFFER_BUTTON) {
                var offer = new ShopOffer(
                    this.getSlot(0).getStack(),
                    this.getSlot(1).getStack(),
                    this.getSlot(2).getStack(),
                    id == SAVE_LOCKED_OFFER_BUTTON
                );

                this.shop.getOffers().set(this.offerIndex, offer);
                this.shop.markDirty();
                this.shop.updateOffers();
                return true;
            }
        }

        return false;
    }

    private void swapOffers(int sourceIndex, int offset) {
        int targetIndex = sourceIndex + offset;

        var sourceOffer = this.shop.getOffers().get(sourceIndex);
        var targetOffer = this.shop.getOffers().get(targetIndex);

        this.shop.getOffers().set(targetIndex, sourceOffer);
        this.shop.getOffers().set(sourceIndex, targetOffer);
        this.shop.markDirty();
    }

    private void updateOfferSlots() {
        var offer = this.getSelectedOffer();

        for (int i = 0; i < 3; i++) {
            var slot = this.getSlot(i);
            if (slot instanceof GhostSlot ghostSlot) {
                ghostSlot.setEnabled(offer != null);
                ghostSlot.setStack(ItemStack.EMPTY);
            }
        }

        if (offer != null) {
            this.getSlot(0).setStack(offer.getFirstBuyItem());
            this.getSlot(1).setStack(offer.getSecondBuyItem());
            this.getSlot(2).setStack(offer.getSellItem());
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.shop.isActivePlayerOwner();
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        // Disables the double click merge stack thing
        return false;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < 3 && this.getSlot(slotIndex) instanceof GhostSlot ghostSlot) {
            // Clear slot on middle click
            if (button == 2) {
                ghostSlot.setStack(ItemStack.EMPTY);
                return;
            }

            if (actionType == SlotActionType.PICKUP) {
                var cursorStack = this.getCursorStack();
                var slotStack = ghostSlot.getStack();

                if (cursorStack.isEmpty()) {
                    // Clear slot or decrement stack by 1
                    if (button == 0) {
                        ghostSlot.setStack(ItemStack.EMPTY);
                    } else if (button == 1) {
                        slotStack.decrement((int)Math.ceil(slotStack.getCount() / 2.0));
                        ghostSlot.setStack(slotStack);
                    }
                } else if (ItemStack.canCombine(slotStack, cursorStack)) {
                    // Increase or set the stack
                    if (button == 1) {
                        slotStack.increment(1);
                    } else {
                        slotStack.setCount(cursorStack.getCount());
                    }
                    ghostSlot.setStack(slotStack);
                } else {
                    // Replace the stack
                    var cursorStackCopy = cursorStack.copy();
                    if (button == 1) {
                        cursorStackCopy.setCount(1);
                    }
                    ghostSlot.setStack(cursorStackCopy);
                }

                ghostSlot.markDirty();
            }

            return;
        }

        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        var slot = this.getSlot(index);

        if (!slot.hasStack() || slot instanceof GhostSlot || index < 3) {
            return ItemStack.EMPTY;
        }

        var stack = slot.getStack();
        var originalStack = stack.copy();

        if (index < 30) {
            if (!this.insertItem(stack, 30, 39, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 39) {
            if (!this.insertItem(stack, 3, 30, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (stack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        return originalStack;
    }

    @Override
    public void close(PlayerEntity player) {
        // Remove all invalid offers
        if (this.shop.getOffers().removeIf(ShopOffer::isInvalid)) {
            this.shop.markDirty();
        }
        this.shop.setActivePlayer(null);
        super.close(player);
    }
}
