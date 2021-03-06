package dev.bohush.economy;

import dev.bohush.economy.advancement.criterion.ModCriteria;
import dev.bohush.economy.block.ModBlocks;
import dev.bohush.economy.block.entity.ModBlockEntities;
import dev.bohush.economy.command.ModCommands;
import dev.bohush.economy.entity.ModEntities;
import dev.bohush.economy.item.ModItems;
import dev.bohush.economy.recipe.ModRecipeSerializers;
import dev.bohush.economy.recipe.ModRecipeTypes;
import dev.bohush.economy.screen.ModScreens;
import dev.bohush.economy.server.network.ModServerPlayNetworkHandler;
import net.fabricmc.api.ModInitializer;

public class Economy implements ModInitializer {
	public static final String MOD_ID = "economy";

	@Override
	public void onInitialize() {
        ModServerPlayNetworkHandler.registerReceivers();
        ModCommands.registerCommands();

		ModItems.registerItems();

        ModBlocks.registerBlocks();
        ModBlocks.registerBlockItems();
        ModBlockEntities.registerBlockEntities();

        ModEntities.registerEntities();

        ModScreens.registerScreenHandlers();

        ModRecipeSerializers.registerRecipeSerializers();
        ModRecipeTypes.registerRecipeTypes();

        ModCriteria.registerCriteria();
	}
}
