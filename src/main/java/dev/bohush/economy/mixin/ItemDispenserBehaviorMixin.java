package dev.bohush.economy.mixin;

import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemDispenserBehavior.class)
public class ItemDispenserBehaviorMixin {

    @Redirect(
        method = "dispenseSilently",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;split(I)Lnet/minecraft/item/ItemStack;")
    )
    private ItemStack splitCoins(ItemStack stack, int amount) {
        if (CoinPileItem.isCoinPile(stack)) {
            var coinToDispense = CoinPileItem.createStack(CoinPileItem.getHighestCoin(stack));
            CoinPileItem.decrementValue(stack, coinToDispense);

            return coinToDispense;
        }
        return stack.split(amount);
    }
}
