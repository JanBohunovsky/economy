package dev.bohush.economy.item;

import net.minecraft.item.ItemStack;

public class ItemStackHelper {
    public static boolean canCombine(ItemStack stack, ItemStack otherStack) {
        if (isCoinPile(stack, otherStack)) {
            return true;
        }

        return ItemStack.canCombine(stack, otherStack);
    }

    public static boolean isCoinPile(ItemStack stack) {
        return CoinPileItem.isCoinPile(stack);
    }

    public static boolean isCoinPile(ItemStack... stacks) {
        for (var stack : stacks) {
            if (!CoinPileItem.isCoinPile(stack)) {
                return false;
            }
        }
        return true;
    }

    public static void increment(ItemStack targetStack, ItemStack sourceStack) {
        if (isCoinPile(targetStack, sourceStack)) {
            CoinPileItem.incrementValue(targetStack, sourceStack);
        } else {
            targetStack.increment(sourceStack.getCount());
        }
    }

    public static void decrement(ItemStack targetStack, ItemStack sourceStack) {
        if (isCoinPile(targetStack, sourceStack)) {
            CoinPileItem.decrementValue(targetStack, sourceStack);
        } else {
            targetStack.decrement(sourceStack.getCount());
        }
    }
}
