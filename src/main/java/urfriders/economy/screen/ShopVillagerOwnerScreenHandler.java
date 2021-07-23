package urfriders.economy.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import urfriders.economy.shop.OwnerShopInventory;
import urfriders.economy.shop.Shop;
import urfriders.economy.shop.ShopInventory;
import urfriders.economy.shop.SimpleShop;

public class ShopVillagerOwnerScreenHandler extends ShopVillagerBaseScreenHandler {

    public ShopVillagerOwnerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleShop(playerInventory.player));
        syncScreenHandler(buf);
    }

    public ShopVillagerOwnerScreenHandler(int syncId, PlayerInventory playerInventory, Shop shop) {
        super(ModScreens.SHOP_VILLAGER_OWNER, syncId, playerInventory, shop);
    }

    @Override
    protected ShopInventory createShopInventory(Shop shop) {
        return new OwnerShopInventory(shop);
    }

    @Override
    protected Slot createInputSlot(ShopInventory shopInventory, int index, int x, int y) {
        return new Slot(shopInventory, index, x, y);
    }

    @Override
    protected Slot createOutputSlot(Shop shop, ShopInventory shopInventory, int index, int x, int y) {
        return new Slot(shopInventory, index, x, y);
    }

    @Override
    protected void offerIndexChanged(int index) {

    }
}
