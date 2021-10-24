package dev.bohush.economy.mixin;

import dev.bohush.economy.item.CoinPileItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public class HandledScreenMixin<T extends ScreenHandler> {
    @Shadow private int heldButtonType;
    @Shadow protected boolean cursorDragging;
    @Shadow @Final protected Set<Slot> cursorDragSlots;
    @Shadow @Final protected T handler;

    private long draggedCoinRemainder;

    @Redirect(
        method = "mouseDragged",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getCount()I",
            ordinal = 1
        )
    )
    private int getCoins(ItemStack stack) {
        if (!CoinPileItem.isCoinPile(stack) || this.heldButtonType == 2) {
            return stack.getCount();
        }

        if (this.heldButtonType == 0) {
            var value = CoinPileItem.getValue(stack);
            return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)value;
        } else {
            var coinStack = CoinPileItem.getHighestCoinStack(stack);
            var result = CoinPileItem.getValue(coinStack) / CoinPileItem.getHighestCoin(stack);
            return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)result;
        }
    }

    @Inject(
        method = "calculateOffset",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/screen/ScreenHandler;getCursorStack()Lnet/minecraft/item/ItemStack;",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void calculateRemainingCoins(CallbackInfo ci) {
        var cursorStack = this.handler.getCursorStack();
        if (!CoinPileItem.isCoinPile(cursorStack) || this.heldButtonType == 2) {
            return;
        }

        ci.cancel();

        if (!this.cursorDragging) {
            return;
        }

        var cursorValue =  CoinPileItem.getValue(cursorStack);
        var valuePerSlot = CoinPileItem.calculateStackValue(cursorValue, this.heldButtonType, this.cursorDragSlots.size(), 0);
        this.draggedCoinRemainder = cursorValue - (valuePerSlot * this.cursorDragSlots.size());
    }

    @Redirect(
        method = "drawSlot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/screen/ScreenHandler;calculateStackSize(Ljava/util/Set;ILnet/minecraft/item/ItemStack;I)V"
        )
    )
    private void setSlotCoinValue(Set<Slot> slots, int mode, ItemStack stack, int stackSize, MatrixStack matrices, Slot slot) {
        if (CoinPileItem.isCoinPile(stack) && mode != 2) {
            var totalValue = CoinPileItem.getValue(stack);
            var slotValue = CoinPileItem.getValue(slot.getStack());
            var value = CoinPileItem.calculateStackValue(totalValue, mode, slots.size(), slotValue);

            CoinPileItem.setValue(stack, value);
        } else {
            ScreenHandler.calculateStackSize(slots, mode, stack, stackSize);
        }
    }

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;setCount(I)V",
            ordinal = 1
        )
    )
    private void setCursorStackCoinValueToRemainder(ItemStack stack, int count) {
        if (CoinPileItem.isCoinPile(stack) && this.heldButtonType != 2) {
            CoinPileItem.setValue(stack, this.draggedCoinRemainder);
        } else {
            stack.setCount(count);
        }
    }
}
