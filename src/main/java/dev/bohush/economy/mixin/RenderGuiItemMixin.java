package dev.bohush.economy.mixin;

import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public class RenderGuiItemMixin {
    @ModifyVariable(
        method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private String setCustomCountLabelForCoinPileItemStack(@Nullable String countLabel, TextRenderer renderer, ItemStack stack, int x, int y) {
        if (countLabel == null && CoinPileItem.isCoinPile(stack)) {
            var value = CoinPileItem.getValue(stack);

            if (value >= CoinPileItem.NETHERITE_COIN) {
                var count = CoinPileItem.getNetheriteCoins(stack);
                if (count > 99) {
                    return "99+";
                }
                if (value != CoinPileItem.NETHERITE_COIN) {
                    return String.valueOf(count);
                }
            } else if (value >= CoinPileItem.GOLD_COIN) {
                var count = CoinPileItem.getGoldCoins(stack);
                if (value != CoinPileItem.GOLD_COIN) {
                    return String.valueOf(count);
                }
            } else if (value >= CoinPileItem.IRON_COIN) {
                var count = CoinPileItem.getIronCoins(stack);
                if (value != CoinPileItem.IRON_COIN) {
                    return String.valueOf(count);
                }
            } else if (value >= CoinPileItem.COPPER_COIN) {
                var count = CoinPileItem.getCopperCoins(stack);
                if (value != CoinPileItem.COPPER_COIN) {
                    return String.valueOf(count);
                }
            }
        }

        return countLabel;
    }
}
