package dev.bohush.economy.mixin;

import dev.bohush.economy.item.CoinPileItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow @Final private static int DESPAWN_AGE;
    @Shadow @Final private static int CANNOT_PICK_UP_DELAY;
    @Shadow private int pickupDelay;
    @Shadow private int itemAge;
    @Shadow public abstract ItemStack getStack();
    @Shadow @Nullable public abstract UUID getOwner();

    @Shadow
    private static void merge(ItemEntity targetEntity, ItemStack targetStack, ItemEntity sourceEntity, ItemStack sourceStack) {
    }

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
        method = "canMerge()Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void canMerge(CallbackInfoReturnable<Boolean> cir) {
        if (CoinPileItem.isCoinPile(this.getStack())) {
            cir.setReturnValue(this.isAlive() && this.pickupDelay != CANNOT_PICK_UP_DELAY && this.itemAge < DESPAWN_AGE);
        }
    }

    @Inject(
        method = "canMerge(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void canMerge(ItemStack targetStack, ItemStack sourceStack, CallbackInfoReturnable<Boolean> cir) {
        if (CoinPileItem.isCoinPile(targetStack, sourceStack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "tryMerge(Lnet/minecraft/entity/ItemEntity;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tryMerge(ItemEntity other, CallbackInfo ci) {
        var stack = this.getStack();
        var otherStack = other.getStack();

        if (!CoinPileItem.isCoinPile(stack, otherStack)) {
            return;
        }

        ci.cancel();

        if (Objects.equals(this.getOwner(), other.getOwner())) {
            if (CoinPileItem.getValue(otherStack) < CoinPileItem.getValue(stack)) {
                merge((ItemEntity)((Object)this), stack, other, otherStack);
            } else {
                merge(other, otherStack, (ItemEntity)((Object)this), stack);
            }
        }
    }

    @Inject(
        method = "merge(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void merge(ItemStack targetStack, ItemStack sourceStack, int maxCount, CallbackInfoReturnable<ItemStack> cir) {
        if (CoinPileItem.isCoinPile(targetStack, sourceStack)) {
            var newStack = CoinPileItem.copy(targetStack);
            CoinPileItem.incrementValue(newStack, sourceStack);
            CoinPileItem.setValue(sourceStack, 0);

            cir.setReturnValue(newStack);
        }
    }

    @Inject(
        method = "damage",
        at = @At("HEAD"),
        cancellable = true
    )
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CoinPileItem.isCoinPile(this.getStack()) && source.isFire() && CoinPileItem.isFullyFireproof(this.getStack())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "isFireImmune",
        at = @At("HEAD"),
        cancellable = true
    )
    private void isFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if (CoinPileItem.isCoinPile(this.getStack()) && CoinPileItem.isFullyFireproof(this.getStack())) {
            cir.setReturnValue(true);
        }
    }
}
