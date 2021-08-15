package dev.bohush.economy.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CoinPileItem extends CoinItem {

    public CoinPileItem(int tier) {
        super(tier);
    }

    @Override
    public long getValue() {
        return super.getValue() * 8;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        return TypedActionResult.pass(player.getStackInHand(hand));
    }
}
