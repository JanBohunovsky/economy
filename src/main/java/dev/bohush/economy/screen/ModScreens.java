package dev.bohush.economy.screen;

import dev.bohush.economy.Economy;
import dev.bohush.economy.block.ShopBlock;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreens {
    public static ScreenHandlerType<ShopStorageScreenHandler> SHOP_STORAGE;
    public static ScreenHandlerType<ShopVillagerScreenHandler> SHOP_VILLAGER;
    public static ScreenHandlerType<ShopVillagerOwnerScreenHandler> SHOP_VILLAGER_OWNER;

    public static void registerScreenHandlers() {
        SHOP_STORAGE = ScreenHandlerRegistry.registerExtended(ShopBlock.ID, ShopStorageScreenHandler::new);
        SHOP_VILLAGER = ScreenHandlerRegistry.registerExtended(
            new Identifier(Economy.MOD_ID, "shop_villager"),
            ShopVillagerScreenHandler::new
        );
        SHOP_VILLAGER_OWNER = ScreenHandlerRegistry.registerExtended(
            new Identifier(Economy.MOD_ID, "shop_villager_owner"),
            ShopVillagerOwnerScreenHandler::new
        );
    }
}
