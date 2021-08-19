package dev.bohush.economy.entity;

import dev.bohush.economy.block.entity.ShopBlockEntity;
import dev.bohush.economy.screen.ShopVillagerScreenHandler;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShopVillagerEntity extends MobEntity implements VillagerDataContainer {
    // TODO: Create custom class for this called VillagerStyle and create custom VillagerClothingFeatureRenderer
    //       that supports this new class.
    private static final TrackedData<VillagerData> VILLAGER_DATA = DataTracker.registerData(ShopVillagerEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
    private static final TrackedData<BlockPos> SHOP_POS = DataTracker.registerData(ShopVillagerEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

    protected ShopVillagerEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 0));
        this.dataTracker.startTracking(SHOP_POS, null);
    }

    public static Builder createShopVillagerAttributes() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0D)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0D);
    }

    private ShopBlockEntity getShopBlockEntity() {
        return (ShopBlockEntity)this.world.getBlockEntity(getShopPos());
    }

    @Override
    public VillagerData getVillagerData() {
        return this.dataTracker.get(VILLAGER_DATA);
    }

    @Override
    public void setVillagerData(VillagerData villagerData) {
        this.dataTracker.set(VILLAGER_DATA, villagerData);
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
//            NamedScreenHandlerFactory factory = isOwner
//                ? createOwnerScreenHandlerFactory()
//                : createCustomerScreenHandlerFactory();
            var factory = createScreenHandlerFactory(isOwner);

            if (factory != null) {
                shopBlockEntity.setActivePlayer(player);
                player.openHandledScreen(factory);
            }
        }

        return ActionResult.success(this.world.isClient);
    }

//    private NamedScreenHandlerFactory createOwnerScreenHandlerFactory() {
//        ShopBlockEntity shopBlockEntity = getShopBlockEntity();
//
//        return new ExtendedScreenHandlerFactory() {
//            @Override
//            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
//                shopBlockEntity.prepareOffers();
//                ShopOfferList offers = shopBlockEntity.getOffers();
//                offers.toPacket(buf);
//            }
//
//            @Override
//            public Text getDisplayName() {
//                return new TranslatableText("shop.name.owner");
//            }
//
//            @Override
//            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
//                return new ShopVillagerOwnerScreenHandler(syncId, inv, shopBlockEntity);
//            }
//        };
//    }

    @Nullable
    private NamedScreenHandlerFactory createScreenHandlerFactory(boolean isOwner) {
        ShopBlockEntity shopBlockEntity = getShopBlockEntity();
        if (!isOwner) {
            if (shopBlockEntity.getOffers().isEmpty()) {
                return null;
            }
        }

        return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                shopBlockEntity.prepareOffers();
                ShopOfferList offers = shopBlockEntity.getOffers();
                offers.toPacket(buf);
                buf.writeBoolean(isOwner);
            }

            @Override
            public Text getDisplayName() {
                return ShopVillagerEntity.this.getDisplayName();
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ShopVillagerScreenHandler(syncId, inv, shopBlockEntity, isOwner);
            }
        };
    }

    private void sayNo() {
        // swing head
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
            setShopPos(NbtHelper.toBlockPos(nbt.getCompound("ShopPos")));
        } else {
            throw new RuntimeException("ShopPos is missing. Please only summon this entity by using economy:shop block.");
        }

        this.setCanPickUpLoot(false);
        this.setInvulnerable(true);
        this.setAiDisabled(true);
        this.setPersistent();
    }
}
