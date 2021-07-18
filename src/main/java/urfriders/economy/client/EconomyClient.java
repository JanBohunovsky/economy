package urfriders.economy.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import urfriders.economy.client.screen.TradingStationScreen;
import urfriders.economy.screen.ModScreens;

@Environment(EnvType.CLIENT)
public class EconomyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register screens
        ScreenRegistry.register(ModScreens.TRADING_STATION, TradingStationScreen::new);
    }
}
