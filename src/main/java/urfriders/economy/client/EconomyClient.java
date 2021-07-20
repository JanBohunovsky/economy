package urfriders.economy.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import urfriders.economy.client.network.ModClientPlayNetworkHandler;
import urfriders.economy.client.render.ShopVillagerEntityRenderer;
import urfriders.economy.client.screen.PlayerShopScreen;
import urfriders.economy.client.screen.ShopVillagerScreen;
import urfriders.economy.entity.ModEntities;
import urfriders.economy.screen.ModScreens;

@Environment(EnvType.CLIENT)
public class EconomyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register network receivers
        ModClientPlayNetworkHandler.registerReceivers();

        // Register screens
        ScreenRegistry.register(ModScreens.PLAYER_SHOP, PlayerShopScreen::new);
        ScreenRegistry.register(ModScreens.SHOP_VILLAGER, ShopVillagerScreen::new);

        // Register entity renderers
        EntityRendererRegistry.INSTANCE.register(ModEntities.SHOP_VILLAGER, ShopVillagerEntityRenderer::new);
    }
}
