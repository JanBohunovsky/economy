package dev.bohush.economy.shop.villager;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public enum ProfessionClothes {
    ARMORER,
    BUTCHER,
    CARTOGRAPHER,
    CLERIC(true),
    FARMER,
    FISHERMAN,
    FLETCHER,
    LEATHERWORKER,
    LIBRARIAN,
    MASON,
    NITWIT,
    SHEPHERD,
    TOOLSMITH,
    WEAPONSMITH;

    private final Text displayText;
    private final Identifier texture;
    private final boolean showHat;

    private ProfessionClothes() {
        this(false);
    }

    private ProfessionClothes(boolean showHat) {
        var name = this.name().toLowerCase();
        this.displayText = new TranslatableText("entity.minecraft.villager." + name);
        this.texture = new Identifier(Identifier.DEFAULT_NAMESPACE, "textures/entity/villager/profession/" + name + ".png");
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
