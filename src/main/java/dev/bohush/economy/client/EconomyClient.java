package dev.bohush.economy.client;

import dev.bohush.economy.client.gui.screen.ShopCustomerScreen;
import dev.bohush.economy.client.gui.screen.ShopOwnerScreen;
import dev.bohush.economy.client.gui.screen.ShopStorageScreen;
import dev.bohush.economy.client.network.ModClientPlayNetworkHandler;
import dev.bohush.economy.client.render.entity.model.ModEntityModels;
import dev.bohush.economy.item.CoinPileItem;
import dev.bohush.economy.item.ModItems;
import dev.bohush.economy.screen.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EconomyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModClientPlayNetworkHandler.registerReceivers();
        ModEntityModels.registerModelsAndLayers();

        // TODO: Move this into a method
        // Register screens
        ScreenRegistry.register(ModScreens.SHOP_STORAGE, ShopStorageScreen::new);
        ScreenRegistry.register(ModScreens.SHOP_CUSTOMER, ShopCustomerScreen::new);
        ScreenRegistry.register(ModScreens.SHOP_OWNER, ShopOwnerScreen::new);

        // Register item predicates
        FabricModelPredicateProviderRegistry.register(ModItems.COIN_PILE, new Identifier("copper_coin_amount"), (stack, world, entity, seed) -> {
            return CoinPileItem.getCopperCoins(stack) / 100f;
        });
        FabricModelPredicateProviderRegistry.register(ModItems.COIN_PILE, new Identifier("iron_coin_amount"), (stack, world, entity, seed) -> {
            return CoinPileItem.getIronCoins(stack) / 100f;
        });
        FabricModelPredicateProviderRegistry.register(ModItems.COIN_PILE, new Identifier("gold_coin_amount"), (stack, world, entity, seed) -> {
            return CoinPileItem.getGoldCoins(stack) / 100f;
        });
        FabricModelPredicateProviderRegistry.register(ModItems.COIN_PILE, new Identifier("netherite_coin_amount"), (stack, world, entity, seed) -> {
            return CoinPileItem.getNetheriteCoins(stack) / 100f;
        });
    }
}
