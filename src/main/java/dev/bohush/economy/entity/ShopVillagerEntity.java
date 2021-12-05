package dev.bohush.economy.entity;

import dev.bohush.economy.Economy;
import dev.bohush.economy.block.entity.ShopBlockEntity;
import dev.bohush.economy.entity.ai.goal.LookAtActivePlayerGoal;
import dev.bohush.economy.entity.ai.goal.ShowOffersToPlayerGoal;
import dev.bohush.economy.screen.ShopCustomerScreenHandler;
import dev.bohush.economy.screen.ShopOwnerScreenHandler;
import dev.bohush.economy.shop.villager.ShopVillagerStyle;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ShopVillagerEntity extends MobEntity {
    public static Identifier ID = new Identifier(Economy.MOD_ID, "shop_villager");

    private static final TrackedData<BlockPos> SHOP_POS = DataTracker.registerData(ShopVillagerEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
    private static final TrackedData<Integer> HEAD_ROLLING_TIME_LEFT = DataTracker.registerData(ShopVillagerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public ShopVillagerEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        Arrays.fill(this.armorDropChances, 0);
        Arrays.fill(this.handDropChances, 0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHOP_POS, null);
        this.dataTracker.startTracking(HEAD_ROLLING_TIME_LEFT, 0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new LookAtActivePlayerGoal(this));
        this.goalSelector.add(1, new ShowOffersToPlayerGoal(this, 8));
        this.goalSelector.add(2, new LookAtEntityGoal(this, LivingEntity.class, 8));
        this.goalSelector.add(3, new LookAroundGoal(this));
    }

    public static Builder createShopVillagerAttributes() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0D)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0D);
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return damageSource != DamageSource.OUT_OF_WORLD;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean isImmobile() {
        return false;
    }

    @Override
    public boolean canMoveVoluntarily() {
        return super.canMoveVoluntarily();
    }

    @Override
    public void setVelocity(Vec3d velocity) {
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
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

    public ShopVillagerStyle getStyle() {
        var shopBlockEntity = this.getShopBlockEntity();
        if (shopBlockEntity == null) {
            return ShopVillagerStyle.EMPTY;
        }

        return shopBlockEntity.getVillagerStyle();
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

        return null;
    }

    @Override
    public boolean hasCustomName() {
        return this.getCustomName() != null;
    }

    @Nullable
    @Override
    public Text getCustomName() {
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
        boolean showCustomerScreen = !isOwner || player.isSneaking();
        boolean hasNoOffers = shopBlockEntity.getOffers().isEmpty();

        if (hand == Hand.MAIN_HAND) {
            // No offers for customer -> say no
            if (hasNoOffers && showCustomerScreen) {
                this.sayNo();
            }

            player.incrementStat(Stats.TALKED_TO_VILLAGER);
        }

        // Open screen
        if (!this.world.isClient) {
            var factory = showCustomerScreen
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
        if (shopBlockEntity.getOffers().isEmpty()) {
            return null;
        }

        return new ExtendedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ShopCustomerScreenHandler(syncId, inv, shopBlockEntity);
            }

            @Override
            public Text getDisplayName() {
                if (isOwner && shopBlockEntity.getCustomName() != null) {
                    return shopBlockEntity.getCustomName();
                }

                var ownerName = shopBlockEntity.updateAndGetOwnerName();
                return shopBlockEntity.getCustomName() != null
                    ? shopBlockEntity.getCustomName().shallowCopy().append(" ").append(new TranslatableText("shop.owner", ownerName))
                    : new TranslatableText("shop.name", ownerName);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                shopBlockEntity.toPacket(buf);
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
                shopBlockEntity.toPacket(buf);
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
