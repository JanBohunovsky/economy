package dev.bohush.economy.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CoinPileItem extends BasicItem {
    public static final String COINS_KEY = "Coins";
    public static final long NETHERITE_COIN = 1_000_000;
    public static final long GOLD_COIN = 10_000;
    public static final long IRON_COIN = 100;
    public static final long COPPER_COIN = 1;

    public CoinPileItem() {
        super(new FabricItemSettings().maxCount(1));
    }

    public static boolean isCoinPile(ItemStack stack) {
        return !stack.isEmpty() && stack.isOf(ModItems.COIN_PILE);
    }

    public static boolean isCoinPile(ItemStack... stacks) {
        for (var stack : stacks) {
            if (!isCoinPile(stack)) {
                return false;
            }
        }
        return true;
    }

    public static ItemStack createStack(long value) {
        if (value < 1) {
            return ItemStack.EMPTY;
        }

        var stack = new ItemStack(ModItems.COIN_PILE);
        setValue(stack, value);
        return stack;
    }

    public static List<ItemStack> createSplitStacks(long value) {
        var stacks = new ArrayList<ItemStack>();

        stacks.add(createStack(getNetheriteCoins(value) * NETHERITE_COIN));
        stacks.add(createStack(getGoldCoins(value) * GOLD_COIN));
        stacks.add(createStack(getIronCoins(value) * IRON_COIN));
        stacks.add(createStack(getCopperCoins(value)));

        return stacks;
    }

    public static ItemStack getHighestCoinStack(long value) {
        for (var stack :createSplitStacks(value)) {
            if (!stack.isEmpty()) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack getHighestCoinStack(ItemStack stack) {
        return getHighestCoinStack(getValue(stack));
    }

    public static ItemStack copy(ItemStack stack) {
        return createStack(getValue(stack));
    }

    public static long getValue(ItemStack stack) {
        if (stack.isEmpty() || !stack.isOf(ModItems.COIN_PILE)) {
            return 0;
        }

        var nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(COINS_KEY, NbtElement.LONG_TYPE)) {
            return 0;
        }

        return nbt.getLong(COINS_KEY);
    }

    public static void setValue(ItemStack stack, long value) {
        if (!isCoinPile(stack)) {
            return;
        }

        var nbt = stack.getOrCreateNbt();
        nbt.putLong(COINS_KEY, value);

        if (value <= 0) {
            stack.setCount(0);
        }
    }

    public static void incrementValue(ItemStack stack, long amount) {
        setValue(stack, getValue(stack) + amount);
    }

    public static void decrementValue(ItemStack stack, long amount) {
        incrementValue(stack, -amount);
    }

    public static void incrementValue(ItemStack targetStack, ItemStack sourceStack) {
        incrementValue(targetStack, getValue(sourceStack));
    }

    public static void decrementValue(ItemStack targetStack, ItemStack sourceStack) {
        decrementValue(targetStack, getValue(sourceStack));
    }

    public static long getHighestCoin(long value) {
        return value >= NETHERITE_COIN ? NETHERITE_COIN
            : value >= GOLD_COIN ? GOLD_COIN
            : value >= IRON_COIN ? IRON_COIN
            : value >= COPPER_COIN ? COPPER_COIN
            : 0;
    }

    public static long getHighestCoin(ItemStack stack) {
        return getHighestCoin(getValue(stack));
    }

    /**
     * If this stack contains only one type of coin it returns the value of that coin, otherwise it returns 0.
     */
    public static long getCoinType(long value) {
        if (value % NETHERITE_COIN == 0) {
            return NETHERITE_COIN;
        }
        if (value < NETHERITE_COIN && value % GOLD_COIN == 0) {
            return GOLD_COIN;
        }
        if (value < GOLD_COIN && value % IRON_COIN == 0) {
            return IRON_COIN;
        }
        if (value < IRON_COIN && value > 0) {
            return COPPER_COIN;
        }

        return 0;
    }

    /**
     * If this stack contains only one type of coin it returns the value of that coin, otherwise it returns 0.
     */
    public static long getCoinType(ItemStack stack) {
        return getCoinType(getValue(stack));
    }

    public static long getNetheriteCoins(long value) {
        return value / NETHERITE_COIN;
    }

    public static long getNetheriteCoins(ItemStack stack) {
        return getNetheriteCoins(getValue(stack));
    }

    public static long getGoldCoins(long value) {
        return (value / GOLD_COIN) % 100;
    }

    public static long getGoldCoins(ItemStack stack) {
        return getGoldCoins(getValue(stack));
    }

    public static long getIronCoins(long value) {
        return (value / IRON_COIN) % 100;
    }

    public static long getIronCoins(ItemStack stack) {
        return getIronCoins(getValue(stack));
    }

    public static long getCopperCoins(long value) {
        return value % 100;
    }

    public static long getCopperCoins(ItemStack stack) {
        return getCopperCoins(getValue(stack));
    }

    /**
     * Called when clicking with the ItemStack in cursor.
     *
     * @param cursorStack ItemStack of CoinPileItem in cursor
     * @param slot        The slot that is being clicked on
     * @param clickType   The mouse button that was used
     * @param player      Player
     * @return Whether we want to stop the default behaviour
     */
    @Override
    public boolean onStackClicked(ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player) {
        // Right-click on an empty slot = give 1 of the highest coins
        if (!slot.hasStack() && slot.canInsert(cursorStack) && clickType == ClickType.RIGHT) {
            // while holding control = give the highest coin stack
            if (Screen.hasControlDown()) {
                var stack = getHighestCoinStack(cursorStack);
                decrementValue(cursorStack, stack);
                slot.setStack(stack);

                return true;
            }

            var cursorValue = getValue(cursorStack);
            var amountToGive = getHighestCoin(cursorValue);

            var amountToKeep = cursorValue - amountToGive;
            if (amountToKeep <= 0) {
                return false;
            }

            setValue(cursorStack, amountToKeep);
            slot.setStack(createStack(amountToGive));

            return true;
        }

        // Click on output slot with coins = take all
        if (isCoinPile(slot.getStack()) && !slot.canInsert(cursorStack) && slot.canTakeItems(player)) {
            var slotStack = slot.getStack().copy();
            incrementValue(cursorStack, slotStack);
            slot.takeStack(slotStack.getCount());
            slot.onTakeItem(player, slotStack);

            return true;
        }

        return false;
    }

    /**
     * Called when the ItemStack is clicked in a slot.
     *
     * @param slotStack            ItemStack of CoinPileItem in slot
     * @param cursorStack          ItemStack in cursor
     * @param slot                 The slot that the CoinPileItem is in
     * @param clickType            The mouse button that was used
     * @param player               Player
     * @param cursorStackReference Cursor Stack Reference
     * @return Whether we want to stop the default behaviour
     */
    @Override
    public boolean onClicked(ItemStack slotStack, ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (!slot.canInsert(cursorStack)) {
            return false;
        }

        var slotValue = getValue(slotStack);

        // Left-click while holding control = give the highest coin stack
        // Right-click while holding control = take the highest coin stack
        if (Screen.hasControlDown()) {
            if (clickType == ClickType.LEFT && (cursorStack.isEmpty() || isCoinPile(cursorStack))) {
                var stack = getHighestCoinStack(slotValue);
                decrementValue(slotStack, stack);

                if (cursorStack.isEmpty()) {
                    cursorStackReference.set(stack);
                } else {
                    incrementValue(cursorStack, stack);
                }

                return true;
            }

            if (clickType == ClickType.RIGHT && isCoinPile(cursorStack)) {
                var stack = getHighestCoinStack(cursorStack);
                decrementValue(cursorStack, stack);
                incrementValue(slotStack, stack);

                return true;
            }
        }

        // Right-click with empty cursor stack = take half
        if (cursorStack.isEmpty()) {
            if (clickType == ClickType.LEFT) {
                return false;
            }

            if (slotValue <= 1) {
                return false;
            }

            // Take half, rounded up
            long amountToKeep = slotValue / 2;
            setValue(slotStack, amountToKeep);
            cursorStackReference.set(createStack(slotValue - amountToKeep));

            return true;
        }

        if (!cursorStack.isOf(ModItems.COIN_PILE)) {
            return false;
        }

        var cursorValue = getValue(cursorStack);

        // Left-click on coins with coins = give all
        if (clickType == ClickType.LEFT) {
            setValue(slotStack, slotValue + cursorValue);
            cursorStackReference.set(ItemStack.EMPTY);

            return true;
        }

        // Right-click on coins with coins = give one (of the highest coin)
        var amountToTake = getHighestCoin(cursorValue);
        var amountToKeep = cursorValue - amountToTake;

        if (amountToKeep <= 0) {
            cursorStackReference.set(ItemStack.EMPTY);
        } else {
            setValue(cursorStack, amountToKeep);
        }

        setValue(slotStack, slotValue + amountToTake);

        return true;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return this.getTranslationKey(getValue(stack));
    }

    public String getTranslationKey(long value) {
        var sb = new StringBuilder().append(super.getTranslationKey());
        var type = getCoinType(value);

        if (value <= 0) {
            sb.append(".invalid");
        } else if (type == NETHERITE_COIN) {
            sb.append(".netherite");
        } else if (type == GOLD_COIN) {
            sb.append(".gold");
        } else if (type == IRON_COIN) {
            sb.append(".iron");
        } else if (type == COPPER_COIN) {
            sb.append(".copper");
        }

        return sb.toString();
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        var coinStacks = createSplitStacks(getValue(stack));

        coinStacks.removeIf(ItemStack::isEmpty);
        if (coinStacks.size() <= 1) {
            return Optional.empty();
        }

        return Optional.of(new CoinPileTooltipData(coinStacks));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        var value = getValue(stack);

        tooltip.add(new LiteralText(String.format("Value: %,d", value)).formatted(Formatting.GRAY));

        if (Screen.hasShiftDown() || Screen.hasControlDown()) {
            tooltip.add(new TranslatableText("tooltip.coin_pile.ctrl").formatted(Formatting.ITALIC, Formatting.GRAY));
        }
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (!this.isIn(group)) {
            return;
        }

        stacks.add(createStack(NETHERITE_COIN));
        stacks.add(createStack(GOLD_COIN));
        stacks.add(createStack(IRON_COIN));
        stacks.add(createStack(COPPER_COIN));
    }
}
