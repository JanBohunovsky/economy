package dev.bohush.economy.mixin;

import dev.bohush.economy.entity.ShopVillagerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NameTagItem.class)
public class NameTagItemMixin {
    @Inject(
        method = "useOnEntity",
        at = @At("HEAD"),
        cancellable = true
    )
    private void dontUseOnShopVillagerEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (stack.hasCustomName() && entity instanceof ShopVillagerEntity) {
            if (!user.world.isClient) {
                user.sendMessage(new TranslatableText("shop.renameHint"), true);
            }
            cir.setReturnValue(ActionResult.success(user.world.isClient));
        }
    }
}
