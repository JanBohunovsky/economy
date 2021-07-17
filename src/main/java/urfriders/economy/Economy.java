package urfriders.economy;

import net.fabricmc.api.ModInitializer;
import urfriders.economy.items.ModItems;

public class Economy implements ModInitializer {
	public static final String MOD_ID = "economy";

	@Override
	public void onInitialize() {
		ModItems.registerItems();
	}
}
