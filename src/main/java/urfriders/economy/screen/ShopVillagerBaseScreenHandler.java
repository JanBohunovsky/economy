package urfriders.economy.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import urfriders.economy.shop.Shop;
import urfriders.economy.shop.ShopInventory;
import urfriders.economy.shop.ShopOfferList;

public abstract class ShopVillagerBaseScreenHandler extends ScreenHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    protected final Shop shop;
    protected final ShopInventory shopInventory;
    protected final Property selectedOffer;

    protected ShopVillagerBaseScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Shop shop) {
        super(type, syncId);
        this.selectedOffer = Property.create();
        this.shop = shop;
        this.shopInventory = createShopInventory(shop);

        // Trading slots
        this.addSlot(createInputSlot(shopInventory, 0, 136, 37));
        this.addSlot(createInputSlot(shopInventory, 1, 162, 37));
        this.addSlot(createOutputSlot(this.shop, shopInventory, 2, 220, 37));

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

        this.addProperty(selectedOffer);
    }

    protected void syncScreenHandler(PacketByteBuf buf) {
        shop.setOffersFromServer(ShopOfferList.fromPacket(buf));
    }

    protected abstract ShopInventory createShopInventory(Shop shop);

    protected abstract Slot createInputSlot(ShopInventory shopInventory, int index, int x, int y);

    protected abstract Slot createOutputSlot(Shop shop, ShopInventory shopInventory, int index, int x, int y);

    protected abstract void offerIndexChanged(int index);

    public int getSelectedOffer() {
        return selectedOffer.get();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        LOGGER.info("onButtonClick: id:{}, world:{}", id, player.world.isClient ? "client" : "server");

        if (id >= 0 && id < shop.getOffers().size()) {
            selectedOffer.set(id);
            shopInventory.setOfferIndex(id);
            offerIndexChanged(id);
        }

        return true;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        shopInventory.markDirty();
        super.onContentChanged(inventory);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return shop.getCurrentCustomer() == player;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        shop.setCurrentCustomer(null);
    }

    public ShopOfferList getOffers() {
        return shop.getOffers();
    }
}
