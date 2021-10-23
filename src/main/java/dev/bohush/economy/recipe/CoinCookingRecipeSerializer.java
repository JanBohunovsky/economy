package dev.bohush.economy.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class CoinCookingRecipeSerializer<T extends AbstractCoinCookingRecipe> implements RecipeSerializer<T> {
    private final int cookingTime;
    private final RecipeFactory<T> recipeFactory;

    public CoinCookingRecipeSerializer(RecipeFactory<T> recipeFactory, int cookingTime) {
        this.cookingTime = cookingTime;
        this.recipeFactory = recipeFactory;
    }

    @Override
    public T read(Identifier id, JsonObject json) {
        var group = JsonHelper.getString(json, "group", "");
        var coinType = JsonHelper.getLong(json, "coin_type");

        var result = new Identifier(JsonHelper.getString(json, "result"));
        var resultStack = new ItemStack(Registry.ITEM.getOrEmpty(result).orElseThrow(() -> {
            return new IllegalStateException(String.format("Item: %s does not exist", result));
        }));

        var experience = JsonHelper.getFloat(json, "experience", 0);
        var cookingTime = JsonHelper.getInt(json, "cooking_time", this.cookingTime);

        return this.recipeFactory.create(id, group, coinType, resultStack, experience, cookingTime);
    }

    @Override
    public T read(Identifier id, PacketByteBuf buf) {
        var group = buf.readString();
        var coinType = buf.readVarLong();
        var output = buf.readItemStack();
        var experience = buf.readFloat();
        var cookTime = buf.readVarInt();

        return this.recipeFactory.create(id, group, coinType, output, experience, cookTime);
    }

    @Override
    public void write(PacketByteBuf buf, T recipe) {
        buf.writeString(recipe.getGroup());
        buf.writeVarLong(recipe.getCoinType());
        buf.writeItemStack(recipe.getOutput());
        buf.writeFloat(recipe.getExperience());
        buf.writeVarInt(recipe.getCookTime());
    }

    interface RecipeFactory<T extends AbstractCoinCookingRecipe> {
        T create(Identifier id, String group, long coinType, ItemStack output, float experience, int cookTime);
    }
}
