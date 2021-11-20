package dev.bohush.economy.mixin;

import dev.bohush.economy.advancement.criterion.ModCriteria;
import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryChangedCriterion.class)
public class InventoryChangedCriterionMixin {
    @Inject(
        method = "trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/item/ItemStack;)V",
        at = @At("HEAD")
    )
    private void triggerCoinsChangedCriterion(ServerPlayerEntity player, PlayerInventory inventory, ItemStack stack, CallbackInfo ci) {
        if (CoinPileItem.isCoinPile(stack)) {
            ModCriteria.COINS_CHANGED.trigger(player, stack);
        }
    }
}
