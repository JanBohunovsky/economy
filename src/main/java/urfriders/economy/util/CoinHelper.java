package urfriders.economy.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import urfriders.economy.item.CoinItem;
import urfriders.economy.item.ModItems;

import java.util.ArrayList;

public class CoinHelper {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final CoinItem[] coins = new CoinItem[] {
        ModItems.COPPER_COIN,
        ModItems.IRON_COIN,
        ModItems.GOLD_COIN,
        ModItems.DIAMOND_COIN,
        ModItems.NETHERITE_COIN
    };

    /**
     * Returns an ItemStack with coin of the highest value possible for given value with the remainder.
     */
    public static Pair<ItemStack, Long> getHighestItemStack(long value) {
        for (int i = coins.length - 1; i >= 0; i--) {
            CoinItem coinItem = coins[i];
            if (value >= coinItem.getValue()) {
                int amount = (int)Math.min(value / coinItem.getValue(), coinItem.getMaxCount());
                ItemStack stack = new ItemStack(coinItem, amount);
                long remainder = value - amount * coinItem.getValue();

                LOGGER.info("Returning {} with remainder of {}", stack, remainder);
                return new Pair<>(stack, remainder);
            }
        }

        return new Pair<>(ItemStack.EMPTY, value);
    }

    /**
     * Converts given value into coins.
     */
    public static ArrayList<ItemStack> getItemStacks(long value) {
        ArrayList<ItemStack> result = new ArrayList<>();

        do {
            Pair<ItemStack, Long> coin = getHighestItemStack(value);

            result.add(coin.getLeft());
            value = coin.getRight();
        } while (value > 0);

        return result;
    }
}
