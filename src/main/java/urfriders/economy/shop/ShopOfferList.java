package urfriders.economy.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ShopOfferList extends ArrayList<ShopOffer> {

    @Nullable
    public ShopOffer getValidOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, int index) {
        if (index > 0 && index < size()) {
            ShopOffer offer = get(index);
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

    // TODO: move this to ShopOffer
    public void toPacket(PacketByteBuf buf) {
        buf.writeByte(size() & 255);

        for (ShopOffer offer : this) {
            buf.writeItemStack(offer.getFirstBuyItem());
            buf.writeItemStack(offer.getSellItem());

            ItemStack secondBuyItem = offer.getSecondBuyItem();
            buf.writeBoolean(!secondBuyItem.isEmpty());
            if (!secondBuyItem.isEmpty()) {
                buf.writeItemStack(secondBuyItem);
            }

            buf.writeInt(offer.getTradesLeft());
            buf.writeBoolean(offer.isStorageFull());
            buf.writeBoolean(offer.isManuallyDisabled());
        }
    }

    public static ShopOfferList fromPacket(PacketByteBuf buf) {
        ShopOfferList offerList = new ShopOfferList();
        int count = buf.readByte() & 255;

        for (int i = 0; i < count; i++) {
            ItemStack firstBuyItem = buf.readItemStack();
            ItemStack sellItem = buf.readItemStack();

            ItemStack secondBuyItem = ItemStack.EMPTY;
            if (buf.readBoolean()) {
                secondBuyItem = buf.readItemStack();
            }

            int stock = buf.readInt();

            ShopOffer offer = new ShopOffer(firstBuyItem, secondBuyItem, sellItem, stock);
            offer.setFullStorage(buf.readBoolean());

            if (buf.readBoolean()) {
                offer.disable();
            }

            offerList.add(offer);
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
