package dev.bohush.economy.entity;

import dev.bohush.economy.Economy;
import dev.bohush.economy.block.entity.ShopBlockEntity;
import dev.bohush.economy.screen.ShopCustomerScreenHandler;
import dev.bohush.economy.screen.ShopOwnerScreenHandler;
import dev.bohush.economy.shop.ShopOfferList;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ShopVillagerEntity extends MobEntity {
    public static Identifier ID = new Identifier(Economy.MOD_ID, "shop_villager");

    private static final TrackedData<BlockPos> SHOP_POS = DataTracker.registerData(ShopVillagerEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

    protected ShopVillagerEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        Arrays.fill(this.armorDropChances, 0);
        Arrays.fill(this.handDropChances, 0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHOP_POS, null);
    }

    public static Builder createShopVillagerAttributes() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0D)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0D);
    }

    public int getHeadRollingTimeLeft() {
        return 0; // TODO: this.dataTracker.get(HEAD_ROLLING_TIME_LEFT);
    }

    private ShopBlockEntity getShopBlockEntity() {
        var shopBlockEntity = (ShopBlockEntity)this.world.getBlockEntity(getShopPos());

        if (shopBlockEntity == null) {
            // Oh no, we're about to crash
            // Remove this entity to prevent multiple crashes
            LOGGER.error("Shop block entity not found @ {}", getShopPos());
            this.discard();
        }

        return shopBlockEntity;
    }

    public BlockPos getShopPos() {
        return this.dataTracker.get(SHOP_POS);
    }

    public void setShopPos(BlockPos pos) {
        this.dataTracker.set(SHOP_POS, pos);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ShopBlockEntity shopBlockEntity = getShopBlockEntity();
        if (!this.isAlive() || shopBlockEntity.hasActivePlayer()) {
            return super.interactMob(player, hand);
        }

        boolean isOwner = player.getUuid().equals(shopBlockEntity.getOwnerUuid());
        boolean hasNoOffers = shopBlockEntity.getOffers().isEmpty();
        if (hand == Hand.MAIN_HAND) {
            if (hasNoOffers && !isOwner && !this.world.isClient) {
                sayNo();
            }

            player.incrementStat(Stats.TALKED_TO_VILLAGER);
        }

        if (!this.world.isClient) {
            var factory = !isOwner || player.isSneaking()
                ? createCustomerScreenFactory(isOwner)
                : createOwnerScreenHandlerFactory();

            if (factory != null) {
                shopBlockEntity.setActivePlayer(player);
                player.openHandledScreen(factory);
            }
        }

        return ActionResult.success(this.world.isClient);
    }

    @Nullable
    private NamedScreenHandlerFactory createCustomerScreenFactory(boolean isOwner) {
        ShopBlockEntity shopBlockEntity = getShopBlockEntity();
        if (!isOwner) {
            if (shopBlockEntity.getOffers().isEmpty()) {
                return null;
            }
        }

        return new ExtendedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ShopCustomerScreenHandler(syncId, inv, shopBlockEntity);
            }

            @Override
            public Text getDisplayName() {
                return isOwner
                    ? new TranslatableText("shop.name.owner")
                    : ShopVillagerEntity.this.getDisplayName();
            }

            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                buf.writeUuid(shopBlockEntity.getOwnerUuid());
                shopBlockEntity.prepareOffers();
                ShopOfferList offers = shopBlockEntity.getOffers();
                offers.toPacket(buf);
            }
        };
    }

    private NamedScreenHandlerFactory createOwnerScreenHandlerFactory() {
        ShopBlockEntity shopBlockEntity = getShopBlockEntity();

        return new ExtendedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return new TranslatableText("shop.name.owner");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ShopOwnerScreenHandler(syncId, inv, shopBlockEntity);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                buf.writeUuid(shopBlockEntity.getOwnerUuid());
                shopBlockEntity.prepareOffers();
                ShopOfferList offers = shopBlockEntity.getOffers();
                offers.toPacket(buf);
            }
        };
    }

    private void sayNo() {
        // TODO: swing head
        if (!this.world.isClient) {
            this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
        }
    }

    public void onTrade() {
        this.ambientSoundChance = -this.getMinAmbientSoundDelay();
    }

    public void onSellingItem(ItemStack stack) {
        if (!this.world.isClient && this.ambientSoundChance > 20 - this.getMinAmbientSoundDelay()) {
            this.ambientSoundChance = -this.getMinAmbientSoundDelay();
            this.playSound(
                stack.isEmpty() ? SoundEvents.ENTITY_VILLAGER_NO : SoundEvents.ENTITY_VILLAGER_YES,
                this.getSoundVolume(),
                this.getSoundPitch()
            );
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return getShopBlockEntity().hasActivePlayer()
            ? SoundEvents.ENTITY_VILLAGER_TRADE
            : SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.put("ShopPos", NbtHelper.fromBlockPos(getShopPos()));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.contains("ShopPos", NbtElement.COMPOUND_TYPE)) {
            this.setShopPos(NbtHelper.toBlockPos(nbt.getCompound("ShopPos")));
        } else {
            throw new RuntimeException("ShopPos is missing. Please only summon this entity by using economy:shop block.");
        }

        this.setCanPickUpLoot(false);
        this.setInvulnerable(true);
        this.setAiDisabled(true);
        this.setPersistent();
    }
}
