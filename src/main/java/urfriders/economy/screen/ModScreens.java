package urfriders.economy.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import urfriders.economy.Economy;
import urfriders.economy.block.PlayerShopBlock;

public class ModScreens {
    public static ScreenHandlerType<PlayerShopScreenHandler> PLAYER_SHOP;
    public static ScreenHandlerType<ShopVillagerScreenHandler> SHOP_VILLAGER;

    public static void registerScreenHandlers() {
        PLAYER_SHOP = ScreenHandlerRegistry.registerSimple(PlayerShopBlock.ID, PlayerShopScreenHandler::new);
        SHOP_VILLAGER = ScreenHandlerRegistry.registerExtended(new Identifier(Economy.MOD_ID, "shop_villager"), ShopVillagerScreenHandler::new);
    }
}
