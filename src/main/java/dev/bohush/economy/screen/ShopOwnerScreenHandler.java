package dev.bohush.economy.screen;

import dev.bohush.economy.shop.ClientShop;
import dev.bohush.economy.shop.Shop;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ShopOwnerScreenHandler extends ScreenHandler {

    private final Shop shop;

    public ShopOwnerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new ClientShop(playerInventory.player, buf));
    }

    public ShopOwnerScreenHandler(int syncId, PlayerInventory playerInventory, Shop shop) {
        super(ModScreens.SHOP_OWNER, syncId);
        this.shop = shop;

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
