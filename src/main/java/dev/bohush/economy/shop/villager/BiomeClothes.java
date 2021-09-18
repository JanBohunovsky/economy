package dev.bohush.economy.shop.villager;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public enum BiomeClothes {
    PLAINS,
    DESERT,
    JUNGLE,
    SAVANNA,
    SNOW("block.minecraft."),
    SWAMP,
    TAIGA(true);

    private final Text displayText;
    private final Identifier texture;
    private final boolean showHat;

    private BiomeClothes() {
        this(false);
    }

    private BiomeClothes(boolean showHat) {
        this("biome.minecraft.", showHat);
    }

    private BiomeClothes(String baseText) {
        this(baseText, false);
    }

    private BiomeClothes(String baseText, boolean showHat) {
        var name = this.name().toLowerCase();
        this.displayText = new TranslatableText(baseText + name);
        this.texture = new Identifier(Identifier.DEFAULT_NAMESPACE, "textures/entity/villager/type/" + name + ".png");
        this.showHat = showHat;
    }

    public Text getDisplayText() {
        return this.displayText;
    }

    public Identifier getTexture() {
        return this.texture;
    }

    public boolean showHat() {
        return this.showHat;
    }
}
