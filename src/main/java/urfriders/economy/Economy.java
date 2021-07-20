package urfriders.economy;

import net.fabricmc.api.ModInitializer;
import urfriders.economy.block.ModBlocks;
import urfriders.economy.block.entity.ModBlockEntities;
import urfriders.economy.entity.ModEntities;
import urfriders.economy.item.ModItems;
import urfriders.economy.screen.ModScreens;
import urfriders.economy.server.network.ModServerPlayNetworkHandler;

public class Economy implements ModInitializer {
	public static final String MOD_ID = "economy";

	@Override
	public void onInitialize() {
        ModServerPlayNetworkHandler.registerReceivers();

		ModItems.registerItems();
        ModBlocks.registerBlocks();
        ModBlocks.registerBlockItems();
        ModBlockEntities.registerBlockEntities();
        ModEntities.registerEntities();
        ModScreens.registerScreenHandlers();
	}
}
