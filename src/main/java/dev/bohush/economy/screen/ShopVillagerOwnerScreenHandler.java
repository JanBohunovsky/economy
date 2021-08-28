package dev.bohush.economy.screen;

import dev.bohush.economy.shop.ClientShop;
import dev.bohush.economy.shop.Shop;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public class ShopVillagerOwnerScreenHandler extends ScreenHandler {

    private final Shop shop;

    public ShopVillagerOwnerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new ClientShop(playerInventory.player, buf));
    }

    public ShopVillagerOwnerScreenHandler(int syncId, PlayerInventory playerInventory, Shop shop) {
        super(ModScreens.SHOP_VILLAGER_OWNER, syncId);
        this.shop = shop;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.shop.isActivePlayerOwner();
    }

    @Override
    public void close(PlayerEntity player) {
        shop.setActivePlayer(null);
        super.close(player);
    }
}
