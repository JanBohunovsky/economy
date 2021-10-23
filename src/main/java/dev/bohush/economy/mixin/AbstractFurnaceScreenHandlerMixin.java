package dev.bohush.economy.mixin;

import dev.bohush.economy.recipe.RecipeManagerHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(AbstractFurnaceScreenHandler.class)
public class AbstractFurnaceScreenHandlerMixin {
    @Redirect(
        method = "isSmeltable",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;")
    )
    private Optional<? extends AbstractCookingRecipe> onIsSmeltableGetCoinRecipe(RecipeManager recipeManager, RecipeType<? extends AbstractCookingRecipe> recipeType, Inventory inventory, World world) {
        return RecipeManagerHelper.getFirstCookingMatch(recipeManager, recipeType, inventory, world);
    }
}
