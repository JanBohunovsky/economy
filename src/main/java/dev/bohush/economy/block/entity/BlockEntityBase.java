package dev.bohush.economy.block.entity;

import com.google.common.base.Preconditions;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public abstract class BlockEntityBase extends BlockEntity {

    public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void sync() {
        Preconditions.checkNotNull(this.world);

        if (!(this.world instanceof ServerWorld serverWorld)) {
            throw new IllegalStateException("Cannot call sync() on the logical client! Did you check world.isClient first?");
        }

        serverWorld.getChunkManager().markForUpdate(this.getPos());
    }

    public abstract void toTag(NbtCompound tag);

    public abstract void fromTag(NbtCompound tag);

    public abstract void toClientTag(NbtCompound tag);

    public abstract void fromClientTag(NbtCompound tag);

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        var nbt = super.toInitialChunkDataNbt();
        this.toClientTag(nbt);

        // Mark client tag
        nbt.putBoolean("#c", true);

        return nbt;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        this.toTag(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("#c")) {
            this.fromClientTag(nbt);
        } else {
            this.fromTag(nbt);
        }
    }
}
