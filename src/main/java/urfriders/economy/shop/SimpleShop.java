package urfriders.economy.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SimpleShop implements Shop {
    private final PlayerEntity customer;
    private ShopOfferList offers = new ShopOfferList();

    public SimpleShop(PlayerEntity player) {
        customer = player;
    }

    @Override
    public void setCurrentCustomer(@Nullable PlayerEntity player) {
    }

    @Override
    public @Nullable PlayerEntity getCurrentCustomer() {
        return customer;
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
    public void trade(ShopOffer offer) {
        offer.onTrade();
    }

    @Override
    public void onSellingItem(ItemStack itemStack) {
    }

    @Override
    public World getWorld() {
        return this.customer.world;
    }
}
