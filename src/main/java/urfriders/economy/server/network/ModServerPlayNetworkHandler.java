package urfriders.economy.server.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import urfriders.economy.network.ModPackets;
import urfriders.economy.screen.ShopStorageScreenHandler;
import urfriders.economy.screen.ShopVillagerScreenHandler;

public class ModServerPlayNetworkHandler {

    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.UPDATE_SHOP_C2S, (server, player, handler, buf, responseSender) -> {
            System.out.println("Packet received: " + ModPackets.UPDATE_SHOP_C2S);

            server.execute(() -> {
                if (player.currentScreenHandler instanceof ShopStorageScreenHandler shopStorageScreenHandler) {
//                    playerShopScreenHandler.updateShop(player.getServerWorld(), player);
                } else {
                    System.out.println("update_shop: Current screen handler is not player shop");
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.SELECT_TRADE_C2S, (server, player, handler, buf, responseSender) -> {
            int tradeId = buf.readVarInt();

            server.execute(() -> {
                if (player.currentScreenHandler instanceof ShopVillagerScreenHandler shopVillagerScreenHandler) {
                    shopVillagerScreenHandler.setRecipeIndex(tradeId);
                    shopVillagerScreenHandler.switchTo(tradeId);
                }
            });
        });
    }
}
