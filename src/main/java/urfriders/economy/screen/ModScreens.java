package urfriders.economy.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import urfriders.economy.Economy;
import urfriders.economy.block.ShopBlock;

public class ModScreens {
    public static ScreenHandlerType<ShopStorageScreenHandler> SHOP_STORAGE;
    public static ScreenHandlerType<ShopVillagerCustomerScreenHandler> SHOP_VILLAGER_CUSTOMER;
    public static ScreenHandlerType<ShopVillagerOwnerScreenHandler> SHOP_VILLAGER_OWNER;

    public static void registerScreenHandlers() {
        SHOP_STORAGE = ScreenHandlerRegistry.registerSimple(ShopBlock.ID, ShopStorageScreenHandler::new);
        SHOP_VILLAGER_CUSTOMER = ScreenHandlerRegistry.registerExtended(
            new Identifier(Economy.MOD_ID, "shop_villager_customer"),
            ShopVillagerCustomerScreenHandler::new
        );
        SHOP_VILLAGER_OWNER = ScreenHandlerRegistry.registerExtended(
            new Identifier(Economy.MOD_ID, "shop_villager_owner"),
            ShopVillagerOwnerScreenHandler::new
        );
    }
}
