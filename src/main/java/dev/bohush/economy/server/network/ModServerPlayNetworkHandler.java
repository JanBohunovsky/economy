package dev.bohush.economy.server.network;

import dev.bohush.economy.network.ModPackets;
import dev.bohush.economy.screen.ShopOwnerScreenHandler;
import dev.bohush.economy.shop.villager.ShopVillagerStyle;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ModServerPlayNetworkHandler {

    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.UPDATE_STYLE_C2S, (server, player, handler, buf, responseSender) -> {
            var style = ShopVillagerStyle.fromPacket(buf);

            server.execute(() -> {
                if (player.currentScreenHandler instanceof ShopOwnerScreenHandler shopOwnerScreenHandler) {
                    shopOwnerScreenHandler.shop.setVillagerStyle(style);
                    shopOwnerScreenHandler.shop.markDirty();
                }
            });
        });
    }
}
