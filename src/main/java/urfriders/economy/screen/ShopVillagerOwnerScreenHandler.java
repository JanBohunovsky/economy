package urfriders.economy.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import urfriders.economy.shop.Shop;
import urfriders.economy.shop.SimpleShop;

public class ShopVillagerOwnerScreenHandler extends ScreenHandler {

    public ShopVillagerOwnerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleShop(playerInventory.player));
    }

    public ShopVillagerOwnerScreenHandler(int syncId, PlayerInventory playerInventory, Shop shop) {
        super(ModScreens.SHOP_VILLAGER_OWNER, syncId);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }
}
