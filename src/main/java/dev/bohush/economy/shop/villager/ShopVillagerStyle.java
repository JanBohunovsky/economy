package dev.bohush.economy.shop.villager;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class ShopVillagerStyle {
    public static ShopVillagerStyle EMPTY = new ShopVillagerStyle(null, null, null, null);
    public static ShopVillagerStyle DEFAULT = new ShopVillagerStyle(BiomeClothes.PLAINS, null, null, null);

    @Nullable
    private final BiomeClothes biomeClothes;
    @Nullable
    private final ProfessionClothes professionClothes;
    @Nullable
    private final Hat hat;
    @Nullable
    private final Accessory accessory;

    public ShopVillagerStyle(
        @Nullable BiomeClothes biomeClothes,
        @Nullable ProfessionClothes professionClothes,
        @Nullable Hat hat,
        @Nullable Accessory accessory
    ) {
        this.biomeClothes = biomeClothes;
        this.professionClothes = professionClothes;
        this.hat = hat;
        this.accessory = accessory;
    }

    @Nullable
    public BiomeClothes getBiomeClothes() {
        return this.biomeClothes;
    }

    @Nullable
    public ProfessionClothes getProfessionClothes() {
        return this.professionClothes;
    }

    @Nullable
    public Hat getHat() {
        return this.hat;
    }

    @Nullable
    public Accessory getAccessory() {
        return this.accessory;
    }

    public ShopVillagerStyle withBiomeClothes(BiomeClothes biomeClothes) {
        return new ShopVillagerStyle(biomeClothes, this.professionClothes, this.hat, this.accessory);
    }

    public ShopVillagerStyle withProfessionClothes(ProfessionClothes professionClothes) {
        return new ShopVillagerStyle(this.biomeClothes, professionClothes, this.hat, this.accessory);
    }

    public ShopVillagerStyle withHat(Hat hat) {
        return new ShopVillagerStyle(this.biomeClothes, this.professionClothes, hat, this.accessory);
    }

    public ShopVillagerStyle withAccessory(Accessory accessory) {
        return new ShopVillagerStyle(this.biomeClothes, this.professionClothes, this.hat, accessory);
    }

    public NbtCompound toNbt() {
        var nbt = new NbtCompound();

        if (this.biomeClothes != null) {
            nbt.putString("biome", this.biomeClothes.name().toLowerCase());
        }
        if (this.professionClothes != null) {
            nbt.putString("profession", this.professionClothes.name().toLowerCase());
        }
        if (this.hat != null) {
            nbt.putString("hat", this.hat.name().toLowerCase());
        }
        if (this.accessory != null) {
            nbt.putString("accessory", this.accessory.name().toLowerCase());
        }

        return nbt;
    }

    public static ShopVillagerStyle fromNbt(NbtCompound nbt) {
        BiomeClothes biomeClothes = null;
        ProfessionClothes professionClothes = null;
        Hat hat = null;
        Accessory accessory = null;

        if (nbt.contains("biome")) {
            biomeClothes = BiomeClothes.valueOf(nbt.getString("biome").toUpperCase());
        }
        if (nbt.contains("profession")) {
            professionClothes = ProfessionClothes.valueOf(nbt.getString("profession").toUpperCase());
        }
        if (nbt.contains("hat")) {
            hat = Hat.valueOf(nbt.getString("hat").toUpperCase());
        }
        if (nbt.contains("accessory")) {
            accessory = Accessory.valueOf(nbt.getString("accessory").toUpperCase());
        }

        return new ShopVillagerStyle(biomeClothes, professionClothes, hat, accessory);
    }
}
