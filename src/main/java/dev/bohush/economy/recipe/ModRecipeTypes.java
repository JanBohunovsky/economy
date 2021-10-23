package dev.bohush.economy.recipe;

import dev.bohush.economy.Economy;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModRecipeTypes {
    public static final RecipeType<CoinSmeltingRecipe> COIN_SMELTING = new RecipeType<>() {
        @Override
        public String toString() {
            return "coin_smelting";
        }
    };

    public static final RecipeType<CoinBlastingRecipe> COIN_BLASTING = new RecipeType<>() {
        @Override
        public String toString() {
            return "coin_blasting";
        }
    };

    public static void registerRecipeTypes() {
        Registry.register(Registry.RECIPE_TYPE, new Identifier(Economy.MOD_ID, COIN_SMELTING.toString()), COIN_SMELTING);
        Registry.register(Registry.RECIPE_TYPE, new Identifier(Economy.MOD_ID, COIN_BLASTING.toString()), COIN_BLASTING);
    }
}
