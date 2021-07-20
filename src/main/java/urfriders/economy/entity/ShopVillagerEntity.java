package urfriders.economy.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import urfriders.economy.block.entity.PlayerShopBlockEntity;
import urfriders.economy.screen.ShopVillagerScreenHandler;

public class ShopVillagerEntity extends VillagerEntity {

    private BlockPos shopPos;

    public ShopVillagerEntity(EntityType<? extends VillagerEntity> entityType, World world) {
        super(entityType, world);
    }

    public void setShop(PlayerShopBlockEntity entity) {
        shopPos = entity.getPos();
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        super.afterUsing(offer);

        System.out.println("ShopVillagerEntity: afterUsing called");
    }

    @Override
    public void restock() {
        // Disable restocking just to be safe
    }

    @Override
    public boolean shouldRestock() {
        // Disable restocking just to be safe
        return false;
    }

    public void sendOffers(PlayerEntity player, Text text, int levelProgress) {
        player.openHandledScreen(new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                TradeOfferList tradeOfferList = ShopVillagerEntity.this.getOffers();
                tradeOfferList.toPacket(buf);
                buf.writeVarInt(levelProgress);
                buf.writeVarInt(ShopVillagerEntity.this.getExperience());
                buf.writeBoolean(ShopVillagerEntity.this.isLeveledMerchant());
                buf.writeBoolean(ShopVillagerEntity.this.canRefreshTrades());
            }

            @Override
            public Text getDisplayName() {
                return text;
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ShopVillagerScreenHandler(syncId, inv, ShopVillagerEntity.this);
            }
        });
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.putIntArray("shopPos", new int[] {shopPos.getX(), shopPos.getY(), shopPos.getZ()});
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        int[] shopPosArray = nbt.getIntArray("shopPos");
        shopPos = new BlockPos(shopPosArray[0], shopPosArray[1], shopPosArray[2]);
    }
}
