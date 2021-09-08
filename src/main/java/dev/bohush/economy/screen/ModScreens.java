package dev.bohush.economy.screen;

import dev.bohush.economy.Economy;
import dev.bohush.economy.block.ShopBlock;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreens {
    public static ScreenHandlerType<ShopStorageScreenHandler> SHOP_STORAGE;
    public static ScreenHandlerType<ShopCustomerScreenHandler> SHOP_CUSTOMER;
    public static ScreenHandlerType<ShopOwnerScreenHandler> SHOP_OWNER;

    public static void registerScreenHandlers() {
        SHOP_STORAGE = ScreenHandlerRegistry.registerExtended(ShopBlock.ID, ShopStorageScreenHandler::new);
        SHOP_CUSTOMER = ScreenHandlerRegistry.registerExtended(
            new Identifier(Economy.MOD_ID, "shop_customer"),
            ShopCustomerScreenHandler::new
        );
        SHOP_OWNER = ScreenHandlerRegistry.registerExtended(
            new Identifier(Economy.MOD_ID, "shop_owner"),
            ShopOwnerScreenHandler::new
        );
    }
}
