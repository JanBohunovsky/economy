package dev.bohush.economy.recipe;

import dev.bohush.economy.Economy;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModRecipeSerializers {
    public static final CoinCookingRecipeSerializer<CoinSmeltingRecipe> COIN_SMELTING = new CoinCookingRecipeSerializer<>(CoinSmeltingRecipe::new, 200);
    public static final CoinCookingRecipeSerializer<CoinBlastingRecipe> COIN_BLASTING = new CoinCookingRecipeSerializer<>(CoinBlastingRecipe::new, 100);

    public static void registerRecipeSerializers() {
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(Economy.MOD_ID, "coin_smelting"), COIN_SMELTING);
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(Economy.MOD_ID, "coin_blasting"), COIN_BLASTING);
    }
}
