package dev.bohush.economy.entity.ai.goal;

import dev.bohush.economy.entity.ShopVillagerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ShowOffersToPlayerGoal extends Goal {
    private static final int OFFER_SHOWING_INTERVAL = 40;

    private final ShopVillagerEntity villager;
    private final float range;
    private final TargetPredicate targetPredicate;
    private final List<ItemStack> offers = new ArrayList<>();

    private PlayerEntity player;
    private int offerIndex;
    private int offerShownTicks;
    private int ticksLeft;

    public ShowOffersToPlayerGoal(ShopVillagerEntity villager, float range) {
        this.villager = villager;
        this.range = range;
        this.targetPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance(this.range);
        this.setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public boolean canStart() {
        this.player = this.villager.world.getClosestPlayer(this.targetPredicate, this.villager, this.villager.getX(), this.villager.getEyeY(), this.villager.getZ());
        if (this.player == null) {
            return false;
        }

        this.setupOffers();
        return !this.offers.isEmpty();
    }

    @Override
    public void start() {
        this.offerIndex = 0;
        this.offerShownTicks = 0;
        this.ticksLeft = OFFER_SHOWING_INTERVAL * this.offers.size();

        this.showOffer();
    }

    @Override
    public boolean shouldContinue() {
        if (this.offers.isEmpty()) {
            return false;
        }

        if (!this.player.isAlive()) {
            return false;
        }

        if (this.villager.squaredDistanceTo(this.player) > (this.range * this.range)) {
            return false;
        }

        return this.ticksLeft > 0;
    }

    @Override
    public void stop() {
        this.player = null;
        this.offers.clear();
        this.villager.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        this.villager.getLookControl().lookAt(this.player.getX(), this.player.getEyeY(), this.player.getZ());

        this.offerShownTicks++;
        this.ticksLeft--;

        if (this.offers.size() > 1 && this.offerShownTicks >= OFFER_SHOWING_INTERVAL && this.ticksLeft > 0) {
            this.offerShownTicks = 0;
            this.offerIndex++;
            if (this.offerIndex >= this.offers.size()) {
                this.offerIndex = 0;
            }

            this.showOffer();
        }
    }

    private void setupOffers() {
        this.offers.clear();

        var shop = this.villager.getShopBlockEntity();
        if (shop == null) {
            return;
        }

        shop.prepareOffers();

        for (var offer : shop.getOffers()) {
            if (!offer.isDisabled()) {
                this.offers.add(offer.getSellItem().copy());
            }
        }
    }

    private void showOffer() {
        this.villager.equipStack(EquipmentSlot.MAINHAND, this.offers.get(this.offerIndex));
    }
}
