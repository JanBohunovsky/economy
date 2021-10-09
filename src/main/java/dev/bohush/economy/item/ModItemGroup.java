package dev.bohush.economy.item;

import dev.bohush.economy.Economy;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    public static final ItemGroup ECONOMY = FabricItemGroupBuilder
            .create(new Identifier(Economy.MOD_ID, "all"))
            .icon(() -> new ItemStack(ModItems.MOD_ICON))
            .build();
}
