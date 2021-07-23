package urfriders.economy.entity;

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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import urfriders.economy.block.entity.ShopBlockEntity;
import urfriders.economy.screen.ShopVillagerCustomerScreenHandler;
import urfriders.economy.screen.ShopVillagerOwnerScreenHandler;
import urfriders.economy.shop.ShopOfferList;

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
        dataTracker.startTracking(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 0));
        dataTracker.startTracking(SHOP_POS, null);
    }

    public static Builder createShopVillagerAttributes() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0D)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0D);
    }

    private ShopBlockEntity getShopBlockEntity() {
        return (ShopBlockEntity)world.getBlockEntity(getShopPos());
    }

    @Override
    public VillagerData getVillagerData() {
        return dataTracker.get(VILLAGER_DATA);
    }

    @Override
    public void setVillagerData(VillagerData villagerData) {
        dataTracker.set(VILLAGER_DATA, villagerData);
    }

    public BlockPos getShopPos() {
        return dataTracker.get(SHOP_POS);
    }

    public void setShopPos(BlockPos pos) {
        dataTracker.set(SHOP_POS, pos);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ShopBlockEntity shopBlockEntity = getShopBlockEntity();
        if (!this.isAlive() || shopBlockEntity.hasCustomer()) {
            return super.interactMob(player, hand);
        }

        boolean isOwner = player.getUuid().equals(shopBlockEntity.getOwnerUuid());
        boolean hasNoOffers = shopBlockEntity.getOffers().isEmpty();
        if (hand == Hand.MAIN_HAND) {
            if (hasNoOffers && !isOwner && !world.isClient) {
                sayNo();
            }

            player.incrementStat(Stats.TALKED_TO_VILLAGER);
        }

        if (!world.isClient) {
            NamedScreenHandlerFactory factory = isOwner
                ? createOwnerScreenHandlerFactory()
                : createCustomerScreenHandlerFactory();

            if (factory != null) {
                shopBlockEntity.setCurrentCustomer(player);
                player.openHandledScreen(factory);
            }
        }

        return ActionResult.success(world.isClient);
    }

    private NamedScreenHandlerFactory createOwnerScreenHandlerFactory() {
        ShopBlockEntity shopBlockEntity = getShopBlockEntity();

        return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                ShopOfferList offers = shopBlockEntity.getOffers();
                offers.toPacket(buf);
            }

            @Override
            public Text getDisplayName() {
                return new TranslatableText("shop.name.owner");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ShopVillagerOwnerScreenHandler(syncId, inv, shopBlockEntity);
            }
        };
    }

    @Nullable
    private NamedScreenHandlerFactory createCustomerScreenHandlerFactory() {
        ShopBlockEntity shopBlockEntity = getShopBlockEntity();
        if (shopBlockEntity.getOffers().isEmpty()) {
            return null;
        }

        return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                ShopOfferList offers = shopBlockEntity.getOffers();
                offers.toPacket(buf);
            }

            @Override
            public Text getDisplayName() {
                return ShopVillagerEntity.this.getDisplayName();
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ShopVillagerCustomerScreenHandler(syncId, inv, shopBlockEntity);
            }
        };
    }

    private void sayNo() {
        // swing head
        if (!world.isClient) {
            this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
        }
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
