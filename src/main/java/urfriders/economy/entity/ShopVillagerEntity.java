package urfriders.economy.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import urfriders.economy.block.entity.PlayerShopBlockEntity;

public class ShopVillagerEntity extends MobEntity implements VillagerDataContainer {

    // TODO: Create custom class for this called VillagerStyle and create custom VillagerClothingFeatureRenderer
    //       that supports this new class.
    private VillagerData villagerData;
    private BlockPos shopPos;

    protected ShopVillagerEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 0);
    }

    public static Builder createShopVillagerAttributes() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0D)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0D);
    }
    @Override
    public VillagerData getVillagerData() {
        return villagerData;
    }

    @Override
    public void setVillagerData(VillagerData villagerData) {
        this.villagerData = villagerData;
    }

    public void setShopPos(BlockPos pos) {
        shopPos = pos;
    }

    private PlayerShopBlockEntity getBlockEntity() {
        return (PlayerShopBlockEntity)world.getBlockEntity(shopPos);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.put("ShopPos", NbtHelper.fromBlockPos(shopPos));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.contains("ShopPos", NbtElement.COMPOUND_TYPE)) {
            shopPos = NbtHelper.toBlockPos(nbt.getCompound("ShopPos"));
        } else {
            throw new RuntimeException("ShopPos is missing. Please only summon this entity by using economy:player_shop block.");
        }

        this.setCanPickUpLoot(false);
        this.setInvulnerable(true);
        this.setAiDisabled(true);
        this.setPersistent();
    }
}
