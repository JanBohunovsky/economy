package dev.bohush.economy.shop;

import dev.bohush.economy.shop.villager.ShopVillagerStyle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClientShop implements Shop {
    private final UUID ownerUuid;
    private final PlayerEntity activePlayer;
    private ShopOfferList offers;
    private ShopVillagerStyle style;

    public ClientShop(PlayerEntity activePlayer, UUID ownerUuid, ShopOfferList offers, ShopVillagerStyle style) {
        this.activePlayer = activePlayer;

        this.ownerUuid = ownerUuid;
        this.offers = offers;
        this.style = style;
    }

    @Override
    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    @Override
    public void setActivePlayer(@Nullable PlayerEntity player) {
    }

    @Override
    public @Nullable PlayerEntity getActivePlayer() {
        return activePlayer;
    }

    @Override
    public ShopOfferList getOffers() {
        return offers;
    }

    @Override
    public void setOffersFromServer(ShopOfferList offers) {
        this.offers = offers;
    }

    @Override
    public void updateOffers() {
    }

    @Override
    public boolean canTrade(ShopOffer offer) {
        return !offer.isDisabled() && !offer.getSellItem().isEmpty();
    }

    @Override
    public void trade(ShopOffer offer) {
        offer.onTrade();
    }

    @Override
    public void onSellingItem(ItemStack stack) {
    }

    @Override
    public World getWorld() {
        return this.activePlayer.world;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public ShopVillagerStyle getVillagerStyle() {
        return style;
    }

    @Override
    public void setVillagerStyle(ShopVillagerStyle style) {
        this.style = style;
    }

    public static ClientShop FromPacket(PlayerEntity activePlayer, PacketByteBuf buf) {
        var ownerUuid = buf.readUuid();
        var offers = ShopOfferList.fromPacket(buf);
        var style = ShopVillagerStyle.fromPacket(buf);

        return new ClientShop(activePlayer, ownerUuid, offers, style);
    }
}
