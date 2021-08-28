package dev.bohush.economy.shop;

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

    public ClientShop(PlayerEntity activePlayer, PacketByteBuf buf) {
        this.activePlayer = activePlayer;

        this.ownerUuid = buf.readUuid();
        this.offers = ShopOfferList.fromPacket(buf);
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
}
