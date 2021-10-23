package dev.bohush.economy.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.world.World;

import java.util.Optional;

public class RecipeManagerHelper {
    public static Optional<? extends AbstractCookingRecipe> getFirstCookingMatch(RecipeManager recipeManager, RecipeType<? extends AbstractCookingRecipe> recipeType, Inventory inventory, World world) {
        var result = recipeManager.getFirstMatch(recipeType, inventory, world);
        if (result.isPresent()) {
            return result;
        }

        if (recipeType == RecipeType.SMELTING) {
            return recipeManager.getFirstMatch(ModRecipeTypes.COIN_SMELTING, inventory, world);
        }

        if (recipeType == RecipeType.BLASTING) {
            return recipeManager.getFirstMatch(ModRecipeTypes.COIN_BLASTING, inventory, world);
        }

        return result;
    }
}
