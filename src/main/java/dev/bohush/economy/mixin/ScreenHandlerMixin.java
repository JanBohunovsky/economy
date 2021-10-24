package dev.bohush.economy.mixin;

import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Shadow @Final public DefaultedList<Slot> slots;
    @Shadow public abstract ItemStack getCursorStack();
    @Shadow public abstract boolean canInsertIntoSlot(ItemStack stack, Slot slot);
    @Shadow @Final public static int EMPTY_SPACE_SLOT_INDEX;
    @Shadow public abstract void setCursorStack(ItemStack stack);
    @Shadow private int quickCraftButton;
    @Shadow @Final private Set<Slot> quickCraftSlots;
    @Shadow public abstract boolean canInsertIntoSlot(Slot slot);
    @Shadow protected abstract void endQuickCraft();

    @Inject(
        method = "internalOnSlotClick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSlotClickHandleCoins(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (actionType == SlotActionType.PICKUP_ALL && slotIndex >= 0) {
            var cursorStack = this.getCursorStack();
            if (!CoinPileItem.isCoinPile(cursorStack)) {
                return;
            }

            ci.cancel();

            var slotUnderCursor = this.slots.get(slotIndex);
            if (!slotUnderCursor.hasStack() || !slotUnderCursor.canTakeItems(player)) {
                for (Slot slot : this.slots) {
                    if (slot.hasStack()
                        && CoinPileItem.isCoinPile(slot.getStack())
                        && slot.canTakeItems(player)
                        && this.canInsertIntoSlot(cursorStack, slot)) {
                        var slotStack = slot.takeStack(slot.getStack().getCount());
                        CoinPileItem.incrementValue(cursorStack, slotStack);
                    }
                }
            }
        } else if (actionType == SlotActionType.THROW && this.getCursorStack().isEmpty() && slotIndex >= 0) {
            var slot = this.slots.get(slotIndex);
            var slotStack = slot.getStack();

            if (!CoinPileItem.isCoinPile(slotStack)) {
                return;
            }

            ci.cancel();

            if (!slot.canTakeItems(player)) {
                return;
            }

            if (button == 1 || !slot.canTakePartial(player)) {
                // Drop all the coins if the player is holding control OR we cannot take partial items from the slot
                var stack = slot.takeStack(slotStack.getCount());
                slot.onTakeItem(player, stack);
                player.dropItem(stack, true);
            } else {
                // Drop the highest coin
                var amount = CoinPileItem.getHighestCoin(slotStack);
                CoinPileItem.decrementValue(slotStack, amount);

                var stack = CoinPileItem.createStack(amount);
                slot.onTakeItem(player, stack);
                player.dropItem(stack, true);
            }
        } else if (actionType == SlotActionType.PICKUP && slotIndex == EMPTY_SPACE_SLOT_INDEX && (button == 0 || button == 1)) {
            var cursorStack = this.getCursorStack();
            if (!CoinPileItem.isCoinPile(cursorStack)) {
                return;
            }

            ci.cancel();

            if (button == 0) {
                player.dropItem(cursorStack, true);
                this.setCursorStack(ItemStack.EMPTY);
            } else {
                var amount = CoinPileItem.getHighestCoin(cursorStack);
                CoinPileItem.decrementValue(cursorStack, amount);
                player.dropItem(CoinPileItem.createStack(amount), true);
            }
        } else if (actionType == SlotActionType.CLONE && player.getAbilities().creativeMode && this.getCursorStack().isEmpty() && slotIndex >= 0) {
            var slot = this.slots.get(slotIndex);
            var slotStack = slot.getStack();

            if (!CoinPileItem.isCoinPile(slotStack)) {
                return;
            }

            var type = CoinPileItem.getCoinType(slotStack);
            if (type <= 0) {
                return;
            }

            ci.cancel();
            this.setCursorStack(CoinPileItem.createStack(type * 99));
        }
    }

    @Inject(
        method = "insertItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onInsertItemMergeCoins(ItemStack stack, int startIndex, int endIndex, boolean fromLast, CallbackInfoReturnable<Boolean> cir) {
        if (!CoinPileItem.isCoinPile(stack)) {
            return;
        }

        cir.setReturnValue(false);

        final int initial = fromLast ? endIndex - 1 : startIndex;
        final int delta = fromLast ? -1 : 1;

        // Try to insert into existing slot with coins
        for (int i = initial; i >= startIndex && i < endIndex; i += delta) {
            var slot = this.slots.get(i);
            var slotStack = slot.getStack();

            if (!CoinPileItem.isCoinPile(slotStack) || !slot.canInsert(stack)) {
                continue;
            }

            CoinPileItem.incrementValue(slotStack, stack);
            CoinPileItem.setValue(stack, 0);
            slot.markDirty();

            cir.setReturnValue(true);
            return;
        }

        // Try to insert into empty slot
        for (int i = initial; i >= startIndex && i < endIndex; i += delta) {
            var slot = this.slots.get(i);

            if (slot.hasStack() || !slot.canInsert(stack)) {
                continue;
            }

            slot.setStack(CoinPileItem.copy(stack));
            CoinPileItem.setValue(stack, 0);
            slot.markDirty();

            cir.setReturnValue(true);
            return;
        }
    }

    @Inject(
        method = "canInsertItemIntoSlot",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void canInsertCoinIntoSlot(Slot slot, ItemStack stack, boolean allowOverflow, CallbackInfoReturnable<Boolean> cir) {
        if (slot != null && CoinPileItem.isCoinPile(slot.getStack(), stack)) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(
        method = "internalOnSlotClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getCount()I",
            ordinal = 0
        )
    )
    private int getCoins(ItemStack stack) {
        if (!CoinPileItem.isCoinPile(stack) || this.quickCraftButton == 2) {
            return stack.getCount();
        }

        if (this.quickCraftButton == 0) {
            var value = CoinPileItem.getValue(stack);
            return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)value;
        } else {
            var coinStack = CoinPileItem.getHighestCoinStack(stack);
            var result = CoinPileItem.getValue(coinStack) / CoinPileItem.getHighestCoin(stack);
            return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)result;
        }
    }

    @Inject(
        method = "internalOnSlotClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;",
            ordinal = 0,
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    private void handleQuickCraftStage2ForCoins(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        var cursorStack = this.getCursorStack().copy();
        if (!CoinPileItem.isCoinPile(cursorStack) || this.quickCraftButton == 2) {
            return;
        }

        ci.cancel();
        var cursorValue = CoinPileItem.getValue(cursorStack);
        var remainingValue = cursorValue;

        for (var slot : this.quickCraftSlots) {
            if (getCoins(cursorStack) < this.quickCraftSlots.size()) {
                break;
            }
            if (slot == null) {
                continue;
            }
            if (!ScreenHandler.canInsertItemIntoSlot(slot, cursorStack, true)) {
                continue;
            }
            if (!slot.canInsert(cursorStack)) {
                continue;
            }
            if (!this.canInsertIntoSlot(slot)) {
                continue;
            }

            var slotValue = CoinPileItem.getValue(slot.getStack());
            var value = CoinPileItem.calculateStackValue(cursorValue, this.quickCraftButton, this.quickCraftSlots.size(), slotValue);

            remainingValue -= value - slotValue;
            slot.setStack(CoinPileItem.createStack(value));
        }

        this.setCursorStack(CoinPileItem.createStack(remainingValue));
        this.endQuickCraft();
    }
}
