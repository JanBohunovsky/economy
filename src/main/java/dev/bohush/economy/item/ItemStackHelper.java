package dev.bohush.economy.item;

import net.minecraft.item.ItemStack;

public class ItemStackHelper {
    public static boolean canCombine(ItemStack stack, ItemStack otherStack) {
        if (CoinPileItem.isCoinPile(stack, otherStack)) {
            return true;
        }

        return ItemStack.canCombine(stack, otherStack);
    }

    public static void increment(ItemStack targetStack, ItemStack sourceStack) {
        if (CoinPileItem.isCoinPile(targetStack, sourceStack)) {
            CoinPileItem.incrementValue(targetStack, sourceStack);
        } else {
            targetStack.increment(sourceStack.getCount());
        }
    }

    public static void decrement(ItemStack targetStack, ItemStack sourceStack) {
        if (CoinPileItem.isCoinPile(targetStack, sourceStack)) {
            CoinPileItem.decrementValue(targetStack, sourceStack);
        } else {
            targetStack.decrement(sourceStack.getCount());
        }
    }
}
