package urfriders.economy;

import net.fabricmc.api.ModInitializer;
import urfriders.economy.block.ModBlocks;
import urfriders.economy.block.entity.ModBlockEntities;
import urfriders.economy.item.ModItems;
import urfriders.economy.network.ModNetworking;
import urfriders.economy.screen.ModScreens;

public class Economy implements ModInitializer {
	public static final String MOD_ID = "economy";

	@Override
	public void onInitialize() {
        ModNetworking.registerServerReceivers();

		ModItems.registerItems();
        ModBlocks.registerBlocks();
        ModBlocks.registerBlockItems();
        ModBlockEntities.registerBlockEntities();
        ModScreens.registerScreenHandlers();
	}
}
