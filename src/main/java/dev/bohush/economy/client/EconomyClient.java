package dev.bohush.economy.client;

import dev.bohush.economy.client.gui.screen.ShopCustomerScreen;
import dev.bohush.economy.client.gui.screen.ShopOwnerScreen;
import dev.bohush.economy.client.gui.screen.ShopStorageScreen;
import dev.bohush.economy.client.network.ModClientPlayNetworkHandler;
import dev.bohush.economy.client.render.ShopVillagerEntityRenderer;
import dev.bohush.economy.entity.ModEntities;
import dev.bohush.economy.screen.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

@Environment(EnvType.CLIENT)
public class EconomyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register network receivers
        ModClientPlayNetworkHandler.registerReceivers();

        // Register screens
        ScreenRegistry.register(ModScreens.SHOP_STORAGE, ShopStorageScreen::new);
        ScreenRegistry.register(ModScreens.SHOP_CUSTOMER, ShopCustomerScreen::new);
        ScreenRegistry.register(ModScreens.SHOP_OWNER, ShopOwnerScreen::new);

        // Register entity renderers
        EntityRendererRegistry.INSTANCE.register(ModEntities.SHOP_VILLAGER, ShopVillagerEntityRenderer::new);
    }
}
