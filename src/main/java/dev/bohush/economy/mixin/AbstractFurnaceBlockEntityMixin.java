package dev.bohush.economy.mixin;

import dev.bohush.economy.item.CoinPileItem;
import dev.bohush.economy.recipe.AbstractCoinCookingRecipe;
import dev.bohush.economy.recipe.RecipeManagerHelper;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
    @Shadow
    private static boolean canAcceptRecipeOutput(@Nullable Recipe<?> recipe, DefaultedList<ItemStack> slots, int count) {
        return false;
    }

    @Redirect(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;")
    )
    private static Optional<? extends AbstractCookingRecipe> onTickGetCoinRecipe(RecipeManager recipeManager, RecipeType<? extends AbstractCoinCookingRecipe> recipeType, Inventory inventory, World world) {
        return RecipeManagerHelper.getFirstCookingMatch(recipeManager, recipeType, inventory, world);
    }

    @Redirect(
        method = "getCookTime",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;")
    )
    private static Optional<? extends AbstractCookingRecipe> onGetCookTimeGetCoinRecipe(RecipeManager recipeManager, RecipeType<? extends AbstractCookingRecipe> recipeType, Inventory inventory, World world) {
        return RecipeManagerHelper.getFirstCookingMatch(recipeManager, recipeType, inventory, world);
    }

    @Inject(
        method = "craftRecipe",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void craftCoinRecipe(@Nullable Recipe<?> recipe, DefaultedList<ItemStack> slots, int count, CallbackInfoReturnable<Boolean> cir) {
        if (!(recipe instanceof AbstractCoinCookingRecipe coinCookingRecipe)) {
            return;
        }

        if (!canAcceptRecipeOutput(recipe, slots, count)) {
            cir.setReturnValue(false);
            return;
        }

        var resultStack = recipe.getOutput().copy();
        var outputStack = slots.get(2);

        if (outputStack.isEmpty()) {
            slots.set(2, resultStack);
        } else if (outputStack.isOf(resultStack.getItem())) {
            outputStack.increment(1);
        }

        var inputStack = slots.get(0);
        CoinPileItem.decrementValue(inputStack, coinCookingRecipe.getCoinType());

        cir.setReturnValue(true);
    }
}
