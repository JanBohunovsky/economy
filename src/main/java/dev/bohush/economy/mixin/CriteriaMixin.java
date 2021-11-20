package dev.bohush.economy.mixin;

import dev.bohush.economy.util.CriteriaRegistry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Criteria.class)
public class CriteriaMixin {

    @Shadow
    @Final
    private static Map<Identifier, Criterion<?>> VALUES;

    @Inject(
        method = "<clinit>",
        at = @At("RETURN")
    )
    private static void registerCustomCriteria(CallbackInfo ci) {
        VALUES.putAll(CriteriaRegistry.getCriteria());
    }
}
