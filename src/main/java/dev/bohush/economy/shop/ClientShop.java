package dev.bohush.economy.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ClientShop implements Shop {
    private final PlayerEntity activePlayer;
    private ShopOfferList offers;

    public ClientShop(PlayerEntity player, ShopOfferList offers) {
        this.activePlayer = player;
        this.offers = offers;
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
