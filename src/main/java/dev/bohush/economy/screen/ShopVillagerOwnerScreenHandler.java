package dev.bohush.economy.screen;

import dev.bohush.economy.shop.ClientShop;
import dev.bohush.economy.shop.Shop;
import dev.bohush.economy.shop.ShopOfferList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public class ShopVillagerOwnerScreenHandler extends ScreenHandler {

    public ShopVillagerOwnerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new ClientShop(playerInventory.player, ShopOfferList.fromPacket(buf)));
    }

    public ShopVillagerOwnerScreenHandler(int syncId, PlayerInventory playerInventory, Shop shop) {
        super(ModScreens.SHOP_VILLAGER_OWNER, syncId);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }
}
