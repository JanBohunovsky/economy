package urfriders.economy.items;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import urfriders.economy.Economy;

public class ModGroups {
    public static final ItemGroup ALL = FabricItemGroupBuilder
            .create(new Identifier(Economy.MOD_ID, "all"))
            .icon(() -> new ItemStack(ModItems.GOLD_COIN_PILE))
            .build();
}
