package dev.bohush.economy.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

public class CoinBlastingRecipe extends AbstractCoinCookingRecipe {
    public CoinBlastingRecipe(Identifier id, String group, long coinType, ItemStack output, float experience, int cookTime) {
        super(ModRecipeTypes.COIN_BLASTING, id, group, coinType, output, experience, cookTime);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(Blocks.BLAST_FURNACE);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.COIN_BLASTING;
    }
}
