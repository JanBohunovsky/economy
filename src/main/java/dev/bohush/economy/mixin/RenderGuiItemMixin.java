package dev.bohush.economy.mixin;

import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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
                if (count > 999) {
                    return "999+";
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

    @Redirect(
        method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I")
    )
    private int rescaleWidth(TextRenderer renderer, String string) {
        return (int) (renderer.getWidth(string) * getScale(string));
    }

    @Inject(
        method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void rescaleText(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci, MatrixStack matrixStack, String string) {
        var scale = getScale(string);
        if (scale < 1f) {
            matrixStack.translate(x * (1 - scale), y * (1 - scale) + (1 - scale) * 16, 0);
            matrixStack.scale(scale, scale, scale);
        }
    }

    private static float getScale(String string) {
        return string.length() > 3 ? 0.5f
            : string.length() == 3 ? 0.75f
            : 1f;
    }
}
