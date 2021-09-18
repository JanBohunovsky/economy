package dev.bohush.economy.shop.villager;

import dev.bohush.economy.Economy;
import dev.bohush.economy.entity.ShopVillagerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public enum Accessory {
    ARMORER(true),
    CARTOGRAPHER(true),
    READING_GLASSES,
    WEAPONSMITH(true),
    SUNGLASSES,
    PILLAGER_MASK;

    private final Text displayText;
    private final Identifier texture;

    private Accessory() {
        this(false);
    }

    private Accessory(boolean isProfessionSource) {
        var name = this.name().toLowerCase();
        this.displayText = new TranslatableText("shop_villager.accessory." + name);

        if (!isProfessionSource) {
            this.texture = new Identifier(Economy.MOD_ID, "textures/entity/" + ShopVillagerEntity.ID.getPath() + "/accessory/" + name + ".png");
        } else {
            this.texture = new Identifier(Identifier.DEFAULT_NAMESPACE, "textures/entity/villager/profession/" + name + ".png");
        }
    }

    public Text getDisplayText() {
        return this.displayText;
    }

    public Identifier getTexture() {
        return this.texture;
    }
}
