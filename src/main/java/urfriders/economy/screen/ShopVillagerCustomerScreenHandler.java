package urfriders.economy.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import urfriders.economy.block.entity.ShopBlockEntity;
import urfriders.economy.entity.ShopVillagerEntity;
import urfriders.economy.screen.slot.ShopOutputSlot;
import urfriders.economy.shop.CustomerShopInventory;
import urfriders.economy.shop.Shop;
import urfriders.economy.shop.ShopOfferList;
import urfriders.economy.shop.SimpleShop;

public class ShopVillagerCustomerScreenHandler extends ScreenHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    protected final Shop shop;
    protected final CustomerShopInventory shopInventory;
    protected final Property selectedOffer;

    public ShopVillagerCustomerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleShop(playerInventory.player));
        this.shop.setOffersFromServer(ShopOfferList.fromPacket(buf));
    }

    public ShopVillagerCustomerScreenHandler(int syncId, PlayerInventory playerInventory, Shop shop) {
        super(ModScreens.SHOP_VILLAGER_CUSTOMER, syncId);
        this.selectedOffer = Property.create();
        this.shop = shop;
        this.shopInventory = new CustomerShopInventory(shop);

        // Trading slots
        this.addSlot(new Slot(this.shopInventory, 0, 136, 37));
        this.addSlot(new Slot(this.shopInventory, 1, 162, 37));
        this.addSlot(new ShopOutputSlot(this.shop, this.shopInventory, 2, 220, 37));

        // Player inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 108 + x * 18, 84 + y * 18));
            }
        }

        // Player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 108 + i * 18, 142));
        }

        this.addProperty(this.selectedOffer);
        this.selectedOffer.set(-1);
    }

    public int getSelectedOffer() {
        return this.selectedOffer.get();
    }

    public ShopOfferList getOffers() {
        return this.shop.getOffers();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        LOGGER.info("onButtonClick: id:{}, world:{}", id, player.world.isClient ? "client" : "server");

        if (id >= 0 && id < this.shop.getOffers().size()) {
            this.selectedOffer.set(id);
            this.shopInventory.setOfferIndex(id);
            this.switchTo(id);
        }

        return true;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.shopInventory.markDirty();
        super.onContentChanged(inventory);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.shop.getCurrentCustomer() == player;
    }

    private void playYesSound() {
        if (!this.shop.getWorld().isClient) {
            ShopBlockEntity shopBlockEntity = (ShopBlockEntity)this.shop;
            ShopVillagerEntity villagerEntity = shopBlockEntity.getVillager((ServerWorld)this.shop.getWorld());
            this.shop.getWorld().playSound(
                villagerEntity.getX(),
                villagerEntity.getY(),
                villagerEntity.getZ(),
                SoundEvents.ENTITY_VILLAGER_YES,
                SoundCategory.NEUTRAL,
                1.0F,
                1.0F,
                false
            );
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (index == 2) {
                // Output slot
                if (!insertItem(originalStack, this.shopInventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickTransfer(originalStack, newStack);
                this.playYesSound();
            } else if (index < 2) {
                // Input slots
                if (!insertItem(originalStack, this.shopInventory.size(), this.slots.size(), false)) {
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
        this.shop.setCurrentCustomer(null);
        if (this.shop.getWorld().isClient) {
            return;
        }

        if (!player.isAlive() || player instanceof ServerPlayerEntity serverPlayerEntity && serverPlayerEntity.isDisconnected()) {
            ItemStack firstBuyItem = this.shopInventory.removeStack(0);
            if (!firstBuyItem.isEmpty()) {
                player.dropItem(firstBuyItem, false);
            }

            ItemStack secondBuyItem = this.shopInventory.removeStack(1);
            if (!secondBuyItem.isEmpty()) {
                player.dropItem(secondBuyItem, false);
            }
        } else if (player instanceof ServerPlayerEntity) {
            player.getInventory().offerOrDrop(this.shopInventory.removeStack(0));
            player.getInventory().offerOrDrop(this.shopInventory.removeStack(1));
        }
    }

    private void switchTo(int offerIndex) {
        if (offerIndex < getOffers().size()) {
            // Give the player their items back
            ItemStack firstBuyItem = this.shopInventory.getStack(0);
            if (!firstBuyItem.isEmpty()) {
                if (!insertItem(firstBuyItem, 3, 39, true)) {
                    return;
                }

                this.shopInventory.setStack(0, firstBuyItem);
            }

            ItemStack secondBuyItem = this.shopInventory.getStack(1);
            if (!secondBuyItem.isEmpty()) {
                if (!insertItem(secondBuyItem, 3, 39, true)) {
                    return;
                }

                this.shopInventory.setStack(1, secondBuyItem);
            }

            // Try to fill in the new items
            if (this.shopInventory.getStack(0).isEmpty() && this.shopInventory.getStack(1).isEmpty()) {
                ItemStack newFirstBuyItem = getOffers().get(offerIndex).getFirstBuyItem();
                this.autofill(0, newFirstBuyItem);
                ItemStack newSecondBuyItem = getOffers().get(offerIndex).getSecondBuyItem();
                this.autofill(1, newSecondBuyItem);
            }
        }
    }


    private void autofill(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        for (int i = 3; i < 39; i++) {
            ItemStack inventoryStack = this.slots.get(i).getStack();
            if (!inventoryStack.isEmpty() && ItemStack.canCombine(stack, inventoryStack)) {
                ItemStack shopStack = this.shopInventory.getStack(slot);
                int alreadyInShop = shopStack.isEmpty() ? 0 : shopStack.getCount();
                int toBeMoved = Math.min(stack.getMaxCount() - alreadyInShop, inventoryStack.getCount());

                ItemStack newStack = inventoryStack.copy();
                int newCount = alreadyInShop + toBeMoved;

                inventoryStack.decrement(toBeMoved);
                newStack.setCount(newCount);
                this.shopInventory.setStack(slot, newStack);

                if (newCount >= stack.getMaxCount()) {
                    break;
                }
            }
        }
    }
}
