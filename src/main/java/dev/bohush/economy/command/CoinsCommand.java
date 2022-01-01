package dev.bohush.economy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import java.util.Collection;
import java.util.Locale;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CoinsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(
            literal("coins")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("give")
                    .then(argument("targets", EntityArgumentType.players())
                        .then(argument("amount", LongArgumentType.longArg(1))
                            .executes(context -> giveCoins(
                                context,
                                EntityArgumentType.getPlayers(context, "targets"),
                                LongArgumentType.getLong(context, "amount"))
                            )
                        )
                    )
                )
        );
    }

    public static int giveCoins(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> players, long value) {
        ItemStack coinStack = CoinPileItem.createStack(value);
        var formattedValue = String.format(Locale.US, "%,d", value);

        for (var player : players) {
            if (!player.giveItemStack(coinStack)) {
                player.dropItem(coinStack, false);
            }

            context.getSource().sendFeedback(new TranslatableText("commands.coins.give.success", formattedValue, player.getDisplayName()), true);
        }

        return players.size();
    }
}
