package dev.bohush.economy.mixin;

import dev.bohush.economy.client.gui.tooltip.CoinPileTooltipComponent;
import dev.bohush.economy.item.CoinPileTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
public class ScreenMixin {
    @Redirect(
        method = "method_32635",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/tooltip/TooltipComponent;of(Lnet/minecraft/client/item/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;")
    )
    private static TooltipComponent of(TooltipData data) {
        if (data instanceof CoinPileTooltipData coinPileTooltipData) {
            return new CoinPileTooltipComponent(coinPileTooltipData);
        }
        return TooltipComponent.of(data);
    }
}
