package dev.bohush.economy.block.entity;

import com.mojang.serialization.Dynamic;
import dev.bohush.economy.block.ShopBlock;
import dev.bohush.economy.entity.ModEntities;
import dev.bohush.economy.entity.ShopVillagerEntity;
import dev.bohush.economy.inventory.ShopStorage;
import dev.bohush.economy.network.ModPackets;
import dev.bohush.economy.screen.ShopStorageScreenHandler;
import dev.bohush.economy.shop.Shop;
import dev.bohush.economy.shop.ShopOffer;
import dev.bohush.economy.shop.ShopOfferList;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class ShopBlockEntity extends BlockEntity implements Shop, ExtendedScreenHandlerFactory {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ShopStorage storage;
    private UUID ownerUuid;
    private String ownerName;
    private UUID villagerUuid;
    private VillagerData villagerStyle;
    private PlayerEntity activePlayer;
    private ShopOfferList offers;

    public ShopBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHOP, pos, state);
        this.storage = new ShopStorage(this::canPlayerUse);
        this.storage.addListener((sender) -> this.markDirty());
    }

    public void initialize(PlayerEntity player) {
        this.ownerUuid = player.getUuid();
        this.ownerName = player.getName().asString();
        this.villagerStyle = new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 0);
    }

    public ShopStorage getStorage() {
        return this.storage;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public ShopVillagerEntity getVillager() {
        if (!(world instanceof ServerWorld serverWorld)) {
            return null;
        }

        Entity entity = serverWorld.getEntity(this.villagerUuid);
        if (entity instanceof ShopVillagerEntity villager) {
            return villager;
        }

        LOGGER.error("Villager not found '{}'", this.villagerUuid);
        return null;
    }

    public void spawnVillager(ServerWorld world) {
        if (this.villagerUuid != null) {
            LOGGER.error("Failed to spawn villager: Shop already has a villager ({}).", this.villagerUuid);
            return;
        }

        ShopVillagerEntity villager = ModEntities.SHOP_VILLAGER.create(world);
        if (villager == null) {
            LOGGER.error("Failed to create villager.");
            return;
        }

        villager.setCustomName(new TranslatableText("shop.name", this.ownerName));
        villager.setVillagerData(this.villagerStyle);
        villager.setShopPos(this.pos);

        villager.setCanPickUpLoot(false);
        villager.setInvulnerable(true);
        villager.setAiDisabled(true);
        villager.setPersistent();

        float yaw = world.getBlockState(this.pos).get(ShopBlock.FACING).asRotation();
        villager.refreshPositionAndAngles(this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, yaw, 0);

        boolean success = world.spawnEntity(villager);
        if (!success) {
            LOGGER.error("Could not spawn villager.");
            return;
        }

        LOGGER.info("Spawned villager {}.", villager.getUuidAsString());
        this.villagerUuid = villager.getUuid();
    }

    public void removeVillager() {
        ShopVillagerEntity villager = getVillager();
        if (villager != null) {
            villager.discard();
        }
    }

    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world == null || this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("shop.container");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (!player.getUuid().equals(this.ownerUuid) && !(player.isCreativeLevelTwoOp() || player.isSpectator())) {
            // TODO: Update ownerName
            player.sendMessage(new TranslatableText("shop.differentOwner", this.ownerName), true);
            return null;
        }

        if (!player.isSpectator()) {
            setActivePlayer(null);
        }

        return new ShopStorageScreenHandler(syncId, playerInventory, this.storage);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeLong(this.storage.getCoins());
    }

    @Override
    public void setActivePlayer(@Nullable PlayerEntity player) {
        this.activePlayer = player;
    }

    @Override
    public @Nullable PlayerEntity getActivePlayer() {
        return this.activePlayer;
    }

    public boolean hasActivePlayer() {
        return this.activePlayer != null;
    }

    @Override
    public ShopOfferList getOffers() {
        if (this.offers == null) {
            this.offers = new ShopOfferList();
        }

        return this.offers;
    }

    @Override
    public void setOffersFromServer(ShopOfferList offers) {
    }

    @Override
    public void updateOffers() {
        LOGGER.info("updateOffers()");
        if (!this.hasActivePlayer()) {
            return;
        }

        ArrayList<Pair<Byte, ShopOffer>> offersToUpdate = new ArrayList<>();
        int emptySlots = this.storage.getEmptySlotCount();

        for (int i = 0; i < this.getOffers().size(); i++) {
            ShopOffer offer = this.getOffers().get(i);
            if (offer.update(this.storage, emptySlots)) {
                offersToUpdate.add(new Pair<>((byte) i, offer));
            }
        }

        if (offersToUpdate.isEmpty()) {
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(offersToUpdate.size() & 255);

        for (Pair<Byte, ShopOffer> offer : offersToUpdate) {
            buf.writeByte(offer.getLeft());
            offer.getRight().toPacket(buf);
        }

        ServerPlayNetworking.send((ServerPlayerEntity)this.activePlayer, ModPackets.UPDATE_OFFERS_S2C, buf);
    }

    public void prepareOffers() {
        int emptySlots = this.storage.getEmptySlotCount();

        for (ShopOffer offer : this.getOffers()) {
            offer.update(this.storage, emptySlots);
        }
    }

    public void setOffers(ShopOfferList offers) {
        this.offers = offers;
    }

    @Override
    public boolean canTrade(ShopOffer offer) {
        return !offer.isDisabled()
            && this.storage.hasStack(offer.getSellItem())
            && this.storage.canFit(offer.getFirstBuyItem(), offer.getSecondBuyItem());
    }

    @Override
    public void trade(ShopOffer offer) {
        LOGGER.info("trade");
        offer.onTrade();

        // TODO: Check the returned values from these methods?
        this.storage.addStack(offer.getFirstBuyItem().copy());
        if (!offer.getSecondBuyItem().isEmpty()) {
            this.storage.addStack(offer.getSecondBuyItem().copy());
        }
        this.storage.removeStack(offer.getSellItem().copy());

        getVillager().onTrade();
    }

    @Override
    public void onSellingItem(ItemStack stack) {
        getVillager().onSellingItem(stack);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putUuid("Owner", this.ownerUuid);
        nbt.putString("OwnerName", this.ownerName);
        VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.villagerStyle)
            .resultOrPartial(LOGGER::error)
            .ifPresent((nbtElement -> nbt.put("VillagerStyle", nbtElement)));

        if (this.villagerUuid != null) {
            nbt.putUuid("AssignedVillager", this.villagerUuid);
        }

        ShopOfferList offers = getOffers();
        if (!offers.isEmpty()) {
            nbt.put("Offers", offers.toNbt());
        }

        if (!this.storage.isEmpty() || this.storage.getCoins() > 0) {
            nbt.put("Storage", this.storage.toNbt());
        }

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.ownerUuid = nbt.getUuid("Owner");
        if (nbt.contains("OwnerName", NbtElement.STRING_TYPE)) {
            this.ownerName = nbt.getString("OwnerName");
        } else {
            this.ownerName = "someone else";
        }

        VillagerData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, nbt.get("VillagerStyle")))
            .resultOrPartial(LOGGER::error)
            .ifPresent(villagerData -> this.villagerStyle = villagerData);

        this.villagerUuid = nbt.getUuid("AssignedVillager");

        if (nbt.contains("Offers", NbtElement.LIST_TYPE)) {
            this.offers = ShopOfferList.fromNbt(nbt.getList("Offers", NbtElement.COMPOUND_TYPE));
        }

        if (nbt.contains("Storage", NbtElement.COMPOUND_TYPE)) {
            this.storage.fromNbt(nbt.getCompound("Storage"));
        }
    }
}
