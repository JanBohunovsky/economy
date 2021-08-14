package dev.bohush.economy.client.network;

import dev.bohush.economy.network.ModPackets;
import dev.bohush.economy.screen.ShopVillagerCustomerScreenHandler;
import dev.bohush.economy.shop.ShopOffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Pair;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class ModClientPlayNetworkHandler {

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.UPDATE_OFFERS_S2C, ((client, handler, buf, responseSender) -> {

            ArrayList<Pair<Byte, ShopOffer>> offersToUpdate = new ArrayList<>();
            int count = buf.readByte();

            for (int i = 0; i < count; i++) {
                byte index = buf.readByte();
                ShopOffer offer = ShopOffer.fromPacket(buf);
                offersToUpdate.add(new Pair<>(index, offer));
            }

            client.execute(() -> {
                if (client.player.currentScreenHandler instanceof ShopVillagerCustomerScreenHandler customerScreenHandler) {

                    for (Pair<Byte, ShopOffer> offer : offersToUpdate) {
                        customerScreenHandler.getOffers().set(offer.getLeft(), offer.getRight());
                    }
                }
            });
        }));
    }
}
