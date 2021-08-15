package dev.bohush.economy.item;

import dev.bohush.economy.util.CoinHelper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CoinItem extends BasicItem {

    private final int tier;

    public CoinItem(int tier) {
        super(new FabricItemSettings().group(ModItemGroup.ALL));
        this.tier = tier;
    }

    public int getTier() {
        return this.tier;
    }

    public long getValue() {
        return (long)Math.pow(this.getMaxCount(), this.getTier());
    }

    /**
     * Converts the current coin into the next or previous tier if possible.
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        var originalStack = player.getStackInHand(hand);

        // Upgrade
        if (originalStack.getCount() == this.getMaxCount() && !player.isSneaking()) {
            var nextCoin = CoinHelper.getNextTier(originalStack);
            if (nextCoin == null) {
                return TypedActionResult.pass(originalStack);
            }

            var resultStack = new ItemStack(nextCoin, 1);
            var handStack = exchangeCoinStacks(originalStack, resultStack, this.getMaxCount(), player);

            return TypedActionResult.success(handStack, world.isClient);
        }

        // Downgrade
        var previousCoin = CoinHelper.getPreviousTier(originalStack);
        if (previousCoin == null) {
            return TypedActionResult.pass(originalStack);
        }

        var resultStack = new ItemStack(previousCoin, this.getMaxCount());
        var handStack = exchangeCoinStacks(originalStack, resultStack, 1, player);

        return TypedActionResult.success(handStack, world.isClient);
    }

    private ItemStack exchangeCoinStacks(ItemStack inputStack, ItemStack outputStack, int amount, PlayerEntity player) {
        if (!player.getAbilities().creativeMode) {
            inputStack.decrement(amount);
        }

        if (inputStack.isEmpty()) {
            return outputStack;
        }

        if (!player.getInventory().insertStack(outputStack)) {
            player.dropItem(outputStack, false);
        }

        return inputStack;
    }
}
