package dev.bohush.economy.item;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoinPileItem extends BasicItem {
    public static final String COINS_KEY = "Coins";
    public static final long NETHERITE_COIN = 1_000_000;
    public static final long GOLD_COIN = 10_000;
    public static final long IRON_COIN = 100;
    public static final long COPPER_COIN = 1;

    private static final Logger LOGGER = LogManager.getLogger();

    public CoinPileItem() {
        super(new FabricItemSettings().maxCount(1));
    }

    public static float getVisualAmount(ItemStack stack) {
        // value -> Math.log10(value)
        // 1 -> 0 - copper coin
        // 10 -> 1
        // 100 -> 2 - iron coin
        // 1 000 -> 3
        // 10 000 -> 4 - gold coin
        // 100 000 -> 5
        // 1 000 000 -> 6 - netherite coin
        long value = getValue(stack);
        return (float)(Math.log10(value) / 2);
    }

    public static long getCopperCoins(ItemStack stack) {
        return getValue(stack) % 100;
    }

    public static long getIronCoins(ItemStack stack) {
        return (getValue(stack) / IRON_COIN) % 100;
    }

    public static long getGoldCoins(ItemStack stack) {
        return (getValue(stack) / GOLD_COIN) % 100;
    }

    public static long getNetheriteCoins(ItemStack stack) {
        return getValue(stack) / NETHERITE_COIN;
    }

    public static ItemStack createStack(long value) {
        if (value < 1) {
            value = 1;
        }

        var stack = new ItemStack(ModItems.COIN_PILE);
        setValue(stack, value);
        return stack;
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
        if (stack.isEmpty() || !stack.isOf(ModItems.COIN_PILE)) {
            return;
        }

        var nbt = stack.getOrCreateNbt();
        nbt.putLong(COINS_KEY, value);
    }

    /**
     * Called when clicking with the ItemStack in cursor.
     * @param stack ItemStack of CoinPileItem
     * @param slot The slot that is being clicked on
     * @param clickType The mouse button that was used
     * @param player Player
     * @return Whether we want to stop the default behaviour
     */
    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        LOGGER.info("onStackClicked({}, {}, {}, {})", stack, slot, clickType, player);

        // RightClick + EmptySlot = give 1 of the highest coins

        var value = getValue(stack);

        var slotStack = slot.getStack();
        if (slotStack.isEmpty() && clickType == ClickType.RIGHT) {
            var amountToGive = value >= NETHERITE_COIN ? NETHERITE_COIN
                : value >= GOLD_COIN ? GOLD_COIN
                : value >= IRON_COIN ? IRON_COIN
                : COPPER_COIN;

            var amountToKeep = value - amountToGive;
            if (amountToKeep <= 0) {
                return false;
            }

            setValue(stack, amountToKeep);
            slot.setStack(createStack(amountToGive));

            return true;
        }

        return false;
    }

    /**
     * Called when the ItemStack is clicked.
     * @param stack ItemStack of CoinPileItem
     * @param otherStack ItemStack in cursor
     * @param slot The slot that the CoinPileItem is in
     * @param clickType The mouse button that was used
     * @param player Player
     * @param cursorStackReference Not sure
     * @return Whether we want to stop the default behaviour
     */
    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        LOGGER.info("onClicked({}, {}, {}, {}, {}, {})", stack, otherStack, slot, clickType, player, cursorStackReference);
        var value = getValue(stack);

        // LeftClick + EmptyOther = default (pick up stack)
        // RightClick + EmptyOther = give half
        if (otherStack.isEmpty()) {
            if (clickType == ClickType.LEFT) {
                return false;
            }

            if (value <= 1) {
                return false;
            }

            // Give half
            long amountToKeep = value / 2;
            setValue(stack, amountToKeep);
            cursorStackReference.set(createStack(value - amountToKeep));

            return true;
        }

        // NonCoinPileOther = default (swap stacks)
        if (!otherStack.isOf(ModItems.COIN_PILE)) {
            return false;
        }

        var otherValue = getValue(otherStack);

        // LeftClick = merge
        if (clickType == ClickType.LEFT) {
            setValue(stack, value + otherValue);
            cursorStackReference.set(ItemStack.EMPTY);

            return true;
        }

        // RightClick = take one (of the highest coin)
        var amountToTake = otherValue >= NETHERITE_COIN ? NETHERITE_COIN
            : otherValue >= GOLD_COIN ? GOLD_COIN
            : otherValue >= IRON_COIN ? IRON_COIN
            : COPPER_COIN;
        var amountToKeep = otherValue - amountToTake;

        if (amountToKeep <= 0) {
            cursorStackReference.set(ItemStack.EMPTY);
        } else {
            setValue(otherStack, amountToKeep);
            setValue(stack, value + amountToTake);
        }

        return true;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        var translationKey = super.getTranslationKey(stack);

        var value = getValue(stack);

        if (value <= 0) {
            return translationKey + ".invalid";
        }
        if (value % NETHERITE_COIN == 0) {
            return translationKey + ".netherite";
        }
        if (value < NETHERITE_COIN && value % GOLD_COIN == 0) {
            return translationKey + ".gold";
        }
        if (value < GOLD_COIN && value % IRON_COIN == 0) {
            return translationKey + ".iron";
        }
        if (value < IRON_COIN && value % COPPER_COIN == 0) {
            return translationKey + ".copper";
        }

        return translationKey;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        var keySneak = MinecraftClient.getInstance().options.keySneak;
        var showCoins = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(keySneak).getCode());

        var value = getValue(stack);

        if (showCoins) {
            if (value >= NETHERITE_COIN) {
                var amount = value / NETHERITE_COIN;
                tooltip.add(new LiteralText(String.format("Netherite Coin x%,d ", amount)).formatted(Formatting.DARK_GRAY));
                value -= amount * NETHERITE_COIN;
            }

            if (value >= GOLD_COIN) {
                var amount = value / GOLD_COIN;
                tooltip.add(new LiteralText(String.format("Gold Coin x%,d ", amount)).formatted(Formatting.YELLOW));
                value -= amount * GOLD_COIN;
            }

            if (value >= IRON_COIN) {
                var amount = value / IRON_COIN;
                tooltip.add(new LiteralText(String.format("Iron Coin x%,d ", amount)).formatted(Formatting.WHITE));
                value -= amount * IRON_COIN;
            }

            if (value >= COPPER_COIN) {
                var amount = value / COPPER_COIN;
                tooltip.add(new LiteralText(String.format("Copper Coin x%,d", amount)).formatted(Formatting.GOLD));
            }

            value = getValue(stack);
        }

        tooltip.add(new LiteralText(String.format("Value: %,d", value)).formatted(Formatting.GRAY));

    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (!this.isIn(group)) {
            return;
        }

        stacks.add(createStack(COPPER_COIN));
        stacks.add(createStack(IRON_COIN));
        stacks.add(createStack(GOLD_COIN));
        stacks.add(createStack(NETHERITE_COIN));
    }
}
