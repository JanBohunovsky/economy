package urfriders.economy.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import urfriders.economy.block.entity.ShopBlockEntity;
import urfriders.economy.entity.ShopVillagerEntity;
import urfriders.economy.screen.slot.ShopOutputSlot;
import urfriders.economy.shop.CustomerShopInventory;
import urfriders.economy.shop.Shop;
import urfriders.economy.shop.ShopInventory;
import urfriders.economy.shop.SimpleShop;

public class ShopVillagerCustomerScreenHandler extends ShopVillagerBaseScreenHandler {

    public ShopVillagerCustomerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleShop(playerInventory.player));
        syncScreenHandler(buf);
    }

    public ShopVillagerCustomerScreenHandler(int syncId, PlayerInventory playerInventory, Shop shop) {
        super(ModScreens.SHOP_VILLAGER_CUSTOMER, syncId, playerInventory, shop);
    }

    @Override
    protected ShopInventory createShopInventory(Shop shop) {
        return new CustomerShopInventory(shop);
    }

    @Override
    protected Slot createInputSlot(ShopInventory shopInventory, int index, int x, int y) {
        return new Slot(shopInventory, index, x, y);
    }

    @Override
    protected Slot createOutputSlot(Shop shop, ShopInventory shopInventory, int index, int x, int y) {
        return new ShopOutputSlot(shop, (CustomerShopInventory)shopInventory, index, x, y);
    }

    @Override
    protected void offerIndexChanged(int index) {
        if (index < getOffers().size()) {
            // Give the player their items back
            ItemStack firstBuyItem = shopInventory.getStack(0);
            if (!firstBuyItem.isEmpty()) {
                if (!insertItem(firstBuyItem, 3, 39, true)) {
                    return;
                }

                shopInventory.setStack(0, firstBuyItem);
            }

            ItemStack secondBuyItem = shopInventory.getStack(1);
            if (!secondBuyItem.isEmpty()) {
                if (!insertItem(secondBuyItem, 3, 39, true)) {
                    return;
                }

                shopInventory.setStack(1, secondBuyItem);
            }

            // Try to fill in the new items
            if (shopInventory.getStack(0).isEmpty() && shopInventory.getStack(1).isEmpty()) {
                ItemStack newFirstBuyItem = getOffers().get(index).getFirstBuyItem();
                autofill(0, newFirstBuyItem);
                ItemStack newSecondBuyItem = getOffers().get(index).getSecondBuyItem();
                autofill(1, newSecondBuyItem);
            }
        }
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return false;
    }

    private void playYesSound() {
        if (!shop.getWorld().isClient) {
            ShopBlockEntity shopBlockEntity = (ShopBlockEntity)shop;
            ShopVillagerEntity villagerEntity = shopBlockEntity.getVillager((ServerWorld)shop.getWorld());
            shop.getWorld().playSound(
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
        Slot slot = slots.get(index);

        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            // Output slot
            if (index == 2) {
                if (!insertItem(originalStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickTransfer(originalStack, newStack);
                playYesSound();
            } else if (index != 0 && index != 1) {
                if (index >= 3 && index < 30) {
                    if (!insertItem(originalStack, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !insertItem(originalStack, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(originalStack, 3, 39, false)) {
                return ItemStack.EMPTY;
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
        if (shop.getWorld().isClient) {
            return;
        }

        if (!player.isAlive() || player instanceof ServerPlayerEntity serverPlayerEntity && serverPlayerEntity.isDisconnected()) {
            ItemStack firstBuyItem = shopInventory.removeStack(0);
            if (!firstBuyItem.isEmpty()) {
                player.dropItem(firstBuyItem, false);
            }

            ItemStack secondBuyItem = shopInventory.removeStack(1);
            if (!secondBuyItem.isEmpty()) {
                player.dropItem(secondBuyItem, false);
            }
        } else if (player instanceof ServerPlayerEntity) {
            player.getInventory().offerOrDrop(shopInventory.removeStack(0));
            player.getInventory().offerOrDrop(shopInventory.removeStack(1));
        }
    }

    private void autofill(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        for (int i = 3; i < 39; i++) {
            ItemStack inventoryStack = slots.get(i).getStack();
            if (!inventoryStack.isEmpty() && ItemStack.canCombine(stack, inventoryStack)) {
                ItemStack shopStack = shopInventory.getStack(slot);
                int alreadyInShop = shopStack.isEmpty() ? 0 : shopStack.getCount();
                int toBeMoved = Math.min(stack.getMaxCount() - alreadyInShop, inventoryStack.getCount());

                ItemStack newStack = inventoryStack.copy();
                int newCount = alreadyInShop + toBeMoved;

                inventoryStack.decrement(toBeMoved);
                newStack.setCount(newCount);
                shopInventory.setStack(slot, newStack);

                if (newCount >= stack.getMaxCount()) {
                    break;
                }
            }
        }
    }
}
