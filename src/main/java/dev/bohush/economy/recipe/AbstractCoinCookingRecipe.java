package dev.bohush.economy.recipe;

import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public abstract class AbstractCoinCookingRecipe extends AbstractCookingRecipe {
    protected final long coinType;

    public AbstractCoinCookingRecipe(RecipeType<?> type, Identifier id, String group, long coinType, ItemStack output, float experience, int cookTime) {
        super(type, id, group, Ingredient.ofStacks(CoinPileItem.createStack(coinType)), output, experience, cookTime);
        this.coinType = coinType;
    }

    public long getCoinType() {
        return this.coinType;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        var stack = inventory.getStack(0);
        if (!CoinPileItem.isCoinPile(stack)) {
            return false;
        }

        return CoinPileItem.getHighestCoin(stack) == this.coinType;
    }
}
