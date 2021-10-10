package dev.bohush.economy.mixin;

import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    private static final Logger LOGGER = LogManager.getLogger();

}
