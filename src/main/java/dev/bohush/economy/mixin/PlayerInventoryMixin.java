package dev.bohush.economy.mixin;

import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    private static final Logger LOGGER = LogManager.getLogger();

    @Shadow
    public abstract ItemStack getStack(int slot);

    @Shadow
    public abstract void setStack(int slot, ItemStack stack);

    @Shadow
    public abstract ItemStack getMainHandStack();

    @Inject(
        method = "canStackAddMore",
        at = @At("HEAD"),
        cancellable = true
    )
    private void canAddCoinsToStack(ItemStack existingStack, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (CoinPileItem.isCoinPile(existingStack, stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "addStack(ILnet/minecraft/item/ItemStack;)I",
        at = @At("HEAD"),
        cancellable = true
    )
    private void addCoins(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        var slotStack = this.getStack(slot);
        if (!CoinPileItem.isCoinPile(slotStack, stack)) {
            return;
        }

        CoinPileItem.incrementValue(slotStack, stack);
        slotStack.setCooldown(5);
        cir.setReturnValue(0);
    }

    @ModifyVariable(
        method = "offer",
        at = @At("STORE"),
        ordinal = 1
    )
    private int offerAllCoins(int count, ItemStack stack) {
        if (count == 0 && CoinPileItem.isCoinPile(stack)) {
            return 1;
        }
        return count;
    }

    @Inject(
        method = "dropSelectedItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void dropSelectedCoins(boolean entireStack, CallbackInfoReturnable<ItemStack> cir) {
        var stack = this.getMainHandStack();
        if (CoinPileItem.isCoinPile(stack) && !entireStack) {
            var amount = CoinPileItem.getHighestCoin(stack);
            CoinPileItem.decrementValue(stack, amount);
            cir.setReturnValue(CoinPileItem.createStack(amount));
        }
    }
}
