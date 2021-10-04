package dev.bohush.economy.shop;

import dev.bohush.economy.shop.villager.ShopVillagerStyle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Shop {
    UUID getOwnerUuid();

    default boolean isActivePlayerOwner() {
        if (this.getActivePlayer() == null) {
            return false;
        }

        return this.getActivePlayer().getUuid().equals(this.getOwnerUuid());
    }

    void setActivePlayer(@Nullable PlayerEntity player);

    @Nullable
    PlayerEntity getActivePlayer();

    ShopOfferList getOffers();

    void setOffersFromServer(ShopOfferList offers);

    void updateOffers();

    boolean canTrade(ShopOffer offer);

    void trade(ShopOffer offer);

    void onSellingItem(ItemStack stack);

    World getWorld();

    void markDirty();

    ShopVillagerStyle getVillagerStyle();

    void setVillagerStyle(ShopVillagerStyle style);
}
