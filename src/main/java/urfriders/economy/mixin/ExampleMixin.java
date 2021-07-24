package urfriders.economy.mixin;

import net.minecraft.village.MerchantInventory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantInventory.class)
public class ExampleMixin {
    private static final Logger LOGGER = LogManager.getLogger();

	@Inject(at = @At("HEAD"), method = "setOfferIndex")
	private void setOfferIndex(int index, CallbackInfo info) {
	    LOGGER.info("MerchantInventory.setOfferIndex({})", index);
	}
}
