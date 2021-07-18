package urfriders.economy.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import urfriders.economy.Economy;
import urfriders.economy.screen.TradingStationScreenHandler;

public class ModNetworking {
    public static final Identifier SPAWN_VILLAGER = new Identifier(Economy.MOD_ID, "spawn_villager");

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SPAWN_VILLAGER, (server, player, handler, buf, responseSender) -> {
            System.out.println("Packet received: " + SPAWN_VILLAGER);

            server.execute(() -> {
                if (player.currentScreenHandler instanceof TradingStationScreenHandler tradingStationScreenHandler) {
                    System.out.println("SpawnVillager: Current screen handler is trading station");
                    tradingStationScreenHandler.spawnVillager(player.getServerWorld());
                }
            });
        });
    }
}
