package dev.bohush.economy.mixin;

import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinBrain.class)
public class PiglinBrainMixin {
    @Inject(
        method = "isGoldenItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void isGoldenItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (CoinPileItem.isCoinPile(stack) && CoinPileItem.getGoldCoins(stack) > 0) {
            cir.setReturnValue(true);
        }
    }
}
