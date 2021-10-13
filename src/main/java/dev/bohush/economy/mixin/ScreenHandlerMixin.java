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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Shadow
    @Final
    public DefaultedList<Slot> slots;

    @Shadow
    public abstract ItemStack getCursorStack();

    @Shadow
    public abstract boolean canInsertIntoSlot(ItemStack stack, Slot slot);

    @Inject(
        method = "internalOnSlotClick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSlotClickPickupAllCoinPileItems(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
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
        }
    }
}
