package urfriders.economy.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import urfriders.economy.block.TradingStationBlock;

public class ModScreens {
    public static ScreenHandlerType<TradingStationScreenHandler> TRADING_STATION;

    public static void registerScreenHandlers() {
        TRADING_STATION = ScreenHandlerRegistry.registerSimple(TradingStationBlock.ID, TradingStationScreenHandler::new);
    }
}
