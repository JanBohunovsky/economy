package dev.bohush.economy.shop.villager;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ShopVillagerStyle {
    public static final ShopVillagerStyle EMPTY = new ShopVillagerStyle(null, null, null, null);
    public static final ShopVillagerStyle DEFAULT = new ShopVillagerStyle(BiomeClothes.PLAINS, null, null, null);

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

    public void toPacket(PacketByteBuf buf) {
        buf.writeBoolean(this.biomeClothes != null);
        if (this.biomeClothes != null) {
            buf.writeString(this.biomeClothes.name());
        }

        buf.writeBoolean(this.professionClothes != null);
        if (this.professionClothes != null) {
            buf.writeString(this.professionClothes.name());
        }

        buf.writeBoolean(this.hat != null);
        if (this.hat != null) {
            buf.writeString(this.hat.name());
        }

        buf.writeBoolean(this.accessory != null);
        if (this.accessory != null) {
            buf.writeString(this.accessory.name());
        }
    }

    public static ShopVillagerStyle fromPacket(PacketByteBuf buf) {
        BiomeClothes biomeClothes = null;
        if (buf.readBoolean()) {
            biomeClothes = BiomeClothes.valueOf(buf.readString());
        }

        ProfessionClothes professionClothes = null;
        if (buf.readBoolean()) {
            professionClothes = ProfessionClothes.valueOf(buf.readString());
        }

        Hat hat = null;
        if (buf.readBoolean()) {
            hat = Hat.valueOf(buf.readString());
        }

        Accessory accessory = null;
        if (buf.readBoolean()) {
            accessory = Accessory.valueOf(buf.readString());
        }

        return new ShopVillagerStyle(biomeClothes, professionClothes, hat, accessory);
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
        if (nbt.contains("biome")) {
            biomeClothes = BiomeClothes.valueOf(nbt.getString("biome").toUpperCase());
        }

        ProfessionClothes professionClothes = null;
        if (nbt.contains("profession")) {
            professionClothes = ProfessionClothes.valueOf(nbt.getString("profession").toUpperCase());
        }

        Hat hat = null;
        if (nbt.contains("hat")) {
            hat = Hat.valueOf(nbt.getString("hat").toUpperCase());
        }

        Accessory accessory = null;
        if (nbt.contains("accessory")) {
            accessory = Accessory.valueOf(nbt.getString("accessory").toUpperCase());
        }

        return new ShopVillagerStyle(biomeClothes, professionClothes, hat, accessory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShopVillagerStyle that = (ShopVillagerStyle) o;
        return biomeClothes == that.biomeClothes && professionClothes == that.professionClothes && hat == that.hat && accessory == that.accessory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(biomeClothes, professionClothes, hat, accessory);
    }
}
