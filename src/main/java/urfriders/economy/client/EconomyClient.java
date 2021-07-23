package urfriders.economy.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import urfriders.economy.client.network.ModClientPlayNetworkHandler;
import urfriders.economy.client.render.ShopVillagerEntityRenderer;
import urfriders.economy.client.screen.ShopStorageScreen;
import urfriders.economy.client.screen.ShopVillagerCustomerScreen;
import urfriders.economy.client.screen.ShopVillagerOwnerScreen;
import urfriders.economy.entity.ModEntities;
import urfriders.economy.screen.ModScreens;

@Environment(EnvType.CLIENT)
public class EconomyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register network receivers
        ModClientPlayNetworkHandler.registerReceivers();

        // Register screens
        ScreenRegistry.register(ModScreens.SHOP_STORAGE, ShopStorageScreen::new);
        ScreenRegistry.register(ModScreens.SHOP_VILLAGER_CUSTOMER, ShopVillagerCustomerScreen::new);
        ScreenRegistry.register(ModScreens.SHOP_VILLAGER_OWNER, ShopVillagerOwnerScreen::new);

        // Register entity renderers
        EntityRendererRegistry.INSTANCE.register(ModEntities.SHOP_VILLAGER, ShopVillagerEntityRenderer::new);
    }
}
