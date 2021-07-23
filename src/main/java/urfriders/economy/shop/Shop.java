package urfriders.economy.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface Shop {
    void setCurrentCustomer(@Nullable PlayerEntity player);

    @Nullable
    PlayerEntity getCurrentCustomer();

    ShopOfferList getOffers();

    void setOffersFromServer(ShopOfferList offerList);

    void trade(ShopOffer offer);

    void onSellingItem(ItemStack itemStack);

    World getWorld();
}
