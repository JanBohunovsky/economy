package dev.bohush.economy.entity;

import dev.bohush.economy.Economy;
import dev.bohush.economy.block.entity.ShopBlockEntity;
import dev.bohush.economy.screen.ShopCustomerScreenHandler;
import dev.bohush.economy.screen.ShopOwnerScreenHandler;
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
    private static final TrackedData<Integer> HEAD_ROLLING_TIME_LEFT = DataTracker.registerData(ShopVillagerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    protected ShopVillagerEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        Arrays.fill(this.armorDropChances, 0);
        Arrays.fill(this.handDropChances, 0);

        // TODO: figure out how to do this natively
        this.setCanPickUpLoot(false);
        this.setInvulnerable(true);
        this.setAiDisabled(true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHOP_POS, null);
        this.dataTracker.startTracking(HEAD_ROLLING_TIME_LEFT, 0);
    }

    public static Builder createShopVillagerAttributes() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0D)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getHeadRollingTimeLeft() > 0) {
            this.dataTracker.set(HEAD_ROLLING_TIME_LEFT, this.getHeadRollingTimeLeft() - 1);
        }
    }

    public int getHeadRollingTimeLeft() {
        return this.dataTracker.get(HEAD_ROLLING_TIME_LEFT);
    }

    @Nullable
    public ShopBlockEntity getShopBlockEntity() {
        var shopPos = this.getShopPos();

        if (shopPos != null) {
            var blockEntity = this.world.getBlockEntity(shopPos);
            if (blockEntity instanceof ShopBlockEntity shopBlockEntity) {
                return shopBlockEntity;
            }
        }

        // By default, ShopVillager with no ShopBlock assigned is invalid and will be destroyed if this case happens.
        // But this behaviour can be overridden by setting the villager as persistent.
        if (!this.isPersistent() && !this.world.isClient) {
            this.discard();
        }
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return this.getCustomName() != null;
    }

    @Nullable
    @Override
    public Text getCustomName() {
        var customName = super.getCustomName();
        if (customName != null) {
            return customName;
        }

        var shopBlockEntity = this.getShopBlockEntity();
        if (shopBlockEntity != null) {
            return shopBlockEntity.getShopDisplayName();
        }

        return null;
    }

    @Nullable
    public BlockPos getShopPos() {
        return this.dataTracker.get(SHOP_POS);
    }

    public void setShopPos(@Nullable BlockPos pos) {
        this.dataTracker.set(SHOP_POS, pos);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        var shopBlockEntity = this.getShopBlockEntity();

        // No shop -> say no
        if (shopBlockEntity == null) {
            this.sayNo();
            return ActionResult.success(this.world.isClient);
        }

        // Villager is busy -> ignore
        if (!this.isAlive() || shopBlockEntity.hasActivePlayer()) {
            return super.interactMob(player, hand);
        }

        boolean isOwner = player.getUuid().equals(shopBlockEntity.getOwnerUuid());
        boolean hasNoOffers = shopBlockEntity.getOffers().isEmpty();
        if (hand == Hand.MAIN_HAND) {
            // No offers for customer -> say no
            if (hasNoOffers && !isOwner) {
                this.sayNo();
            }

            player.incrementStat(Stats.TALKED_TO_VILLAGER);
        }

        // Open screen
        if (!this.world.isClient) {
            var factory = !isOwner || player.isSneaking()
                ? createCustomerScreenFactory(shopBlockEntity, isOwner)
                : createOwnerScreenHandlerFactory(shopBlockEntity);

            if (factory != null) {
                shopBlockEntity.setActivePlayer(player);
                player.openHandledScreen(factory);
            }
        }

        return ActionResult.success(this.world.isClient);
    }

    @Nullable
    private NamedScreenHandlerFactory createCustomerScreenFactory(ShopBlockEntity shopBlockEntity, boolean isOwner) {
        if (!isOwner && shopBlockEntity.getOffers().isEmpty()) {
            return null;
        }

        return new ExtendedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ShopCustomerScreenHandler(syncId, inv, shopBlockEntity);
            }

            @Override
            public Text getDisplayName() {
                return isOwner
                    ? shopBlockEntity.getOwnerDisplayName()
                    : shopBlockEntity.getShopDisplayName();
            }

            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                buf.writeUuid(shopBlockEntity.getOwnerUuid());
                shopBlockEntity.prepareOffers();
                var offers = shopBlockEntity.getOffers();
                offers.toPacket(buf);
            }
        };
    }

    private NamedScreenHandlerFactory createOwnerScreenHandlerFactory(ShopBlockEntity shopBlockEntity) {
        return new ExtendedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return new TranslatableText("shop.offer.edit");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ShopOwnerScreenHandler(syncId, inv, shopBlockEntity);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                buf.writeUuid(shopBlockEntity.getOwnerUuid());
                shopBlockEntity.prepareOffers();
                var offers = shopBlockEntity.getOffers();
                offers.toPacket(buf);
            }
        };
    }

    private void sayNo() {
        if (this.world.isClient) {
            return;
        }

        this.dataTracker.set(HEAD_ROLLING_TIME_LEFT, 40);
        this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
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
        var shopBlockEntity = this.getShopBlockEntity();
        if (shopBlockEntity == null) {
            return SoundEvents.ENTITY_VILLAGER_AMBIENT;
        }

        return shopBlockEntity.hasActivePlayer()
            ? SoundEvents.ENTITY_VILLAGER_TRADE
            : SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        var shopPos = this.getShopPos();
        if (shopPos != null) {
            nbt.put("ShopPos", NbtHelper.fromBlockPos(shopPos));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.contains("ShopPos", NbtElement.COMPOUND_TYPE)) {
            this.setShopPos(NbtHelper.toBlockPos(nbt.getCompound("ShopPos")));
        } else {
            LOGGER.warn("ShopVillager has no ShopPos");
        }
    }
}
