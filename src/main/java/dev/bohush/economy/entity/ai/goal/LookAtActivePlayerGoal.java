package dev.bohush.economy.entity.ai.goal;

import dev.bohush.economy.entity.ShopVillagerEntity;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.player.PlayerEntity;

public class LookAtActivePlayerGoal extends LookAtEntityGoal {
    private final ShopVillagerEntity villager;

    public LookAtActivePlayerGoal(ShopVillagerEntity villager) {
        super(villager, PlayerEntity.class,  8);
        this.villager = villager;
    }

    @Override
    public boolean canStart() {
        var shop = this.villager.getShopBlockEntity();
        if (shop == null) {
            return false;
        }

        if (!shop.hasActivePlayer()) {
            return false;
        }

        this.target = shop.getActivePlayer();
        return true;
    }
}
