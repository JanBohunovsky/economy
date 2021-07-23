package urfriders.economy.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ShopInventory implements Inventory {
    private static final Logger LOGGER = LogManager.getLogger();

    protected final Shop shop;
    protected final DefaultedList<ItemStack> inventory;

    protected int offerIndex;

    protected ShopInventory(Shop shop) {
        this.shop = shop;
        this.inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : inventory) {
            if (!item.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack item = Inventories.splitStack(inventory, slot, amount);
        if (!item.isEmpty()) {
            markDirty();
        }

        return item;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }

        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return shop.getCurrentCustomer() == player;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    public void setOfferIndex(int index) {
        offerIndex = index;

        ShopOffer offer = shop.getOffers().get(offerIndex);
        LOGGER.info("Current offer: {} -> {} {}",
            offer.getSecondBuyItem().isEmpty()
                ? offer.getFirstBuyItem().toString()
                : offer.getFirstBuyItem().toString().concat(" + ").concat(offer.getSecondBuyItem().toString()),
            offer.getSellItem().toString(),
            offer.isOutOfStock() ? "/OUT OF STOCK/"
                : offer.isManuallyDisabled() ? "/DISABLED/"
                : "");

        markDirty();
    }
}
