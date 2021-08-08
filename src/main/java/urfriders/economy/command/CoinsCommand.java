package urfriders.economy.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import urfriders.economy.item.CoinItem;
import urfriders.economy.util.CoinHelper;

import java.util.ArrayList;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CoinsCommand {

    public CoinsCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(
            literal("coins")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("get")
                    .then(argument("value", LongArgumentType.longArg(1))
                        .executes(context -> giveCoins(context, LongArgumentType.getLong(context, "value")))
                    )
                )
                .then(literal("value")
                    .executes(context -> sendValue(context))
                )
        );

    }

    public static int giveCoins(CommandContext<ServerCommandSource> context, long value) throws CommandSyntaxException {
        ArrayList<ItemStack> stacks = CoinHelper.getItemStacks(value);
        ServerPlayerEntity player = context.getSource().getPlayer();

        for (ItemStack stack : stacks) {
            if (!player.giveItemStack(stack)) {
                player.dropItem(stack, false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int sendValue(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof CoinItem coinItem)) {
            throw new SimpleCommandExceptionType(new LiteralText("You must have a coin item in hand.")).create();
        }

        long value = coinItem.getValue() * stack.getCount();
        // Example: Your 32 Gold Coin(s) have value of 131,072.
        Text message = new LiteralText(String.format("Your %d ", stack.getCount()))
            .append(coinItem.getName())
            .append(String.format("(s) have value of %,d.", value));
        context.getSource().sendFeedback(message, true);

        return Command.SINGLE_SUCCESS;
    }
}
