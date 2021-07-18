package urfriders.economy;

import net.fabricmc.api.ModInitializer;
import urfriders.economy.block.ModBlocks;
import urfriders.economy.item.ModItems;

public class Economy implements ModInitializer {
	public static final String MOD_ID = "economy";

	@Override
	public void onInitialize() {
		ModItems.registerItems();
        ModBlocks.registerBlocks();
        ModBlocks.registerBlockItems();
	}
}
