package dev.bohush.economy.shop.villager;

import dev.bohush.economy.Economy;
import dev.bohush.economy.entity.ShopVillagerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public enum Hat {
    DESERT(Source.BIOME),
    SAVANNA(Source.BIOME),
    SNOW(Source.BIOME),
    SWAMP(Source.BIOME),
    ARMORER(Source.PROFESSION),
    BUTCHER(Source.PROFESSION),
    FARMER(Source.PROFESSION),
    FISHERMAN(Source.PROFESSION),
    FLETCHER(Source.PROFESSION),
    BOOK,
    SHEPHERD(Source.PROFESSION);

    private final Text displayText;
    private final Identifier texture;

    private Hat() {
        this(null);
    }

    private Hat(@Nullable Source source) {
        var name = this.name().toLowerCase();
        this.displayText = new TranslatableText("shop_villager.hat." + name);

        if (source == null) {
            this.texture = new Identifier(Economy.MOD_ID, "textures/entity/" + ShopVillagerEntity.ID.getPath() + "/hat/" + name + ".png");
        } else {
            this.texture = new Identifier(Identifier.DEFAULT_NAMESPACE, "textures/entity/villager/" + source.getKey() + "/" + name + ".png");
        }
    }

    public Text getDisplayText() {
        return this.displayText;
    }

    public Identifier getTexture() {
        return this.texture;
    }

    private enum Source {
        BIOME("type"),
        PROFESSION("profession");

        private final String key;

        private Source(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }
}
