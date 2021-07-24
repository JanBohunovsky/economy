package urfriders.economy.block.entity;

import com.mojang.serialization.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import urfriders.economy.block.ShopBlock;
import urfriders.economy.entity.ModEntities;
import urfriders.economy.entity.ShopVillagerEntity;
import urfriders.economy.item.ModItems;
import urfriders.economy.screen.ShopStorageScreenHandler;
import urfriders.economy.shop.Shop;
import urfriders.economy.shop.ShopOffer;
import urfriders.economy.shop.ShopOfferList;
import urfriders.economy.shop.ShopStorage;

import java.util.UUID;

public class ShopBlockEntity extends BlockEntity implements Shop, NamedScreenHandlerFactory {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ShopStorage storage;
    private UUID ownerUuid;
    private String ownerName;
    private UUID villagerUuid;
    private VillagerData villagerStyle;
    private PlayerEntity customer;
    private ShopOfferList offers;

    public ShopBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHOP, pos, state);
        this.storage = new ShopStorage(this::canPlayerUse);

        // Maybe change this to ShopStorage.onClose(player) { this.updateOffers(); }
        // Because using markDirty would update offers maybe too often
        this.storage.addListener((sender) -> this.updateOffers());
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

    public ShopVillagerEntity getVillager(ServerWorld world) {
        Entity entity = world.getEntity(this.villagerUuid);
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

        // Position and rotation
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

    public void removeVillager(ServerWorld world) {
        ShopVillagerEntity villager = getVillager(world);
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
            setCurrentCustomer(null);
        }
        return new ShopStorageScreenHandler(syncId, playerInventory, this.storage);
    }

    @Override
    public void setCurrentCustomer(@Nullable PlayerEntity player) {
        this.customer = player;
    }

    @Override
    public @Nullable PlayerEntity getCurrentCustomer() {
        return this.customer;
    }

    public boolean hasCustomer() {
        return this.customer != null;
    }

    @Override
    public ShopOfferList getOffers() {
        if (this.offers == null) {
            this.offers = new ShopOfferList();

            // Example offers
            this.offers.add(new ShopOffer(new ItemStack(ModItems.COPPER_COIN, 1), new ItemStack(Items.DIRT, 64), 64));
            this.offers.add(new ShopOffer(new ItemStack(ModItems.IRON_COIN, 1), new ItemStack(Items.COBBLESTONE, 64), 10));
            this.offers.add(new ShopOffer(new ItemStack(ModItems.GOLD_COIN, 1), new ItemStack(Items.NETHER_STAR, 1), 0));

            ShopOffer offer = new ShopOffer(new ItemStack(Items.STICK, 1), new ItemStack(ModItems.DIAMOND_COIN, 1), 64);
            offer.disable();
            this.offers.add(offer);

            offer = new ShopOffer(new ItemStack(Items.DIRT, 64), new ItemStack(Items.DIRT, 64), new ItemStack(ModItems.COPPER_COIN, 1), 64);
            offer.setFullStorage(true);
            this.offers.add(offer);
        }

        return this.offers;
    }

    public void setOffers(ShopOfferList offers) {
        this.offers = offers;
    }

    @Override
    public void setOffersFromServer(ShopOfferList offerList) {
    }

    @Override
    public void trade(ShopOffer offer) {
        offer.onTrade();
        // TODO: update storage
        updateOffer(offer);

        if (this.world instanceof ServerWorld serverWorld) {
            ShopVillagerEntity villagerEntity = getVillager(serverWorld);
            villagerEntity.ambientSoundChance = -villagerEntity.getMinAmbientSoundDelay();
        }
    }

    @Override
    public void onSellingItem(ItemStack itemStack) {
        // TODO: play sound
    }

    private void updateOffers() {
        LOGGER.info("updateOffers called");
        for (ShopOffer offer : this.getOffers()) {
            updateOffer(offer);
        }
    }

    private void updateOffer(ShopOffer offer) {
        if (offer.update(this.storage)) {
            // TODO: send packet to client
        }
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

        if (!this.storage.isEmpty()) {
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

        if (nbt.contains("Storage", NbtElement.LIST_TYPE)) {
            this.storage.fromNbt(nbt.getList("Storage", NbtElement.COMPOUND_TYPE));
        }
    }
}
