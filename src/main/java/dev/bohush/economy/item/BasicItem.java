package dev.bohush.economy.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;

public class BasicItem extends Item {

    public BasicItem() {
        this(new FabricItemSettings());
    }

    public BasicItem(Settings settings) {
        super(settings.group(ModItemGroup.ALL));
    }
}
