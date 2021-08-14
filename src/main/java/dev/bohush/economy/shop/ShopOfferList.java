package dev.bohush.economy.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ShopOfferList extends ArrayList<ShopOffer> {

    @Nullable
    public ShopOffer getValidOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, int index) {
        if (index >= 0 && index < this.size()) {
            ShopOffer offer = this.get(index);
            return offer.matchesBuyItems(firstBuyItem, secondBuyItem)
                ? offer
                : null;
        }

        for (ShopOffer offer : this) {
            if (offer.matchesBuyItems(firstBuyItem, secondBuyItem)) {
                return offer;
            }
        }

        return null;
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeByte(this.size() & 255);

        for (ShopOffer offer : this) {
            offer.toPacket(buf);
        }
    }

    public static ShopOfferList fromPacket(PacketByteBuf buf) {
        ShopOfferList offerList = new ShopOfferList();
        int count = buf.readByte() & 255;

        for (int i = 0; i < count; i++) {
            offerList.add(ShopOffer.fromPacket(buf));
        }

        return offerList;
    }

    public NbtList toNbt() {
        NbtList nbtList = new NbtList();

        for (ShopOffer offer : this) {
            nbtList.add(offer.toNbt());
        }

        return nbtList;
    }

    public static ShopOfferList fromNbt(NbtList nbtList) {
        ShopOfferList offerList = new ShopOfferList();

        for (int i = 0; i < nbtList.size(); i++) {
            offerList.add(ShopOffer.fromNbt(nbtList.getCompound(i)));
        }

        return offerList;
    }
}
