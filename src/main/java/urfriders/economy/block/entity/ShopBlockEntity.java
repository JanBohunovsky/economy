package urfriders.economy.block.entity;

import com.mojang.serialization.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
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
import net.minecraft.util.collection.DefaultedList;
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
import urfriders.economy.inventory.ImplementedInventory;
import urfriders.economy.item.ModItems;
import urfriders.economy.screen.ShopStorageScreenHandler;
import urfriders.economy.shop.Shop;
import urfriders.economy.shop.ShopOffer;
import urfriders.economy.shop.ShopOfferList;

import java.util.UUID;

public class ShopBlockEntity extends BlockEntity implements Shop, NamedScreenHandlerFactory, ImplementedInventory {
    private static final Logger LOGGER = LogManager.getLogger();

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);

    private UUID ownerUuid;
    private String ownerName;
    private UUID villagerUuid;
    private VillagerData villagerStyle;
    private PlayerEntity customer;
    private ShopOfferList offers;

    public ShopBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHOP, pos, state);
    }

    public void initialize(PlayerEntity player) {
        ownerUuid = player.getUuid();
        ownerName = player.getName().asString();
        villagerStyle = new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 0);
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public ShopVillagerEntity getVillager(ServerWorld world) {
        Entity entity = world.getEntity(villagerUuid);
        if (entity instanceof ShopVillagerEntity villager) {
            return villager;
        }

        LOGGER.error("Villager not found '{}'", villagerUuid);
        return null;
    }

    public void spawnVillager(ServerWorld world) {
        if (villagerUuid != null) {
            LOGGER.error("Failed to spawn villager: Shop already has a villager ({}).", villagerUuid);
            return;
        }

        ShopVillagerEntity villager = ModEntities.SHOP_VILLAGER.create(world);
        if (villager == null) {
            LOGGER.error("Failed to create villager.");
            return;
        }

        villager.setCustomName(new TranslatableText("shop.name", ownerName));
        villager.setVillagerData(villagerStyle);
        villager.setShopPos(pos);

        // Position and rotation
        float yaw = world.getBlockState(pos).get(ShopBlock.FACING).asRotation();
        villager.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, yaw, 0);

        boolean success = world.spawnEntity(villager);
        if (!success) {
            LOGGER.error("Could not spawn villager.");
            return;
        }

        LOGGER.info("Spawned villager {}.", villager.getUuidAsString());
        villagerUuid = villager.getUuid();
    }

    public void removeVillager(ServerWorld world) {
        ShopVillagerEntity villager = getVillager(world);
        if (villager != null) {
            villager.discard();
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (world == null || world.getBlockEntity(pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("shop.container");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (!player.getUuid().equals(ownerUuid) && !(player.isCreativeLevelTwoOp() || player.isSpectator())) {
            // TODO: Update ownerName
            player.sendMessage(new TranslatableText("shop.differentOwner", ownerName), true);
            return null;
        }

        // TODO: Disable the villager UI while the screen is open
        //       This is to prevent any potential duplication glitches.
        return new ShopStorageScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public void setCurrentCustomer(@Nullable PlayerEntity player) {
        customer = player;
    }

    @Override
    public @Nullable PlayerEntity getCurrentCustomer() {
        return customer;
    }

    public boolean hasCustomer() {
        return customer != null;
    }

    @Override
    public ShopOfferList getOffers() {
        if (offers == null) {
            offers = new ShopOfferList();
            offers.add(new ShopOffer(new ItemStack(ModItems.COPPER_COIN, 1), new ItemStack(Items.DIRT, 64), 128));
            offers.add(new ShopOffer(new ItemStack(ModItems.IRON_COIN, 1), new ItemStack(Items.COBBLESTONE, 64), 128));
            offers.add(new ShopOffer(new ItemStack(ModItems.GOLD_COIN, 1), new ItemStack(Items.NETHER_STAR, 1), 0));

            ShopOffer offer = new ShopOffer(new ItemStack(Items.STICK, 1), new ItemStack(ModItems.DIAMOND_COIN, 1), 64);
            offer.disable();
            offers.add(offer);
        }

        return offers;
    }

    public void setOffers(ShopOfferList offers) {
        this.offers = offers;
    }

    @Override
    public void setOffersFromServer(ShopOfferList offerList) {
    }

    @Override
    public void trade(ShopOffer offer) {
        offer.decrementStock();
        // TODO: update storage

        if (world instanceof ServerWorld serverWorld) {
            ShopVillagerEntity villagerEntity = getVillager(serverWorld);
            villagerEntity.ambientSoundChance = -villagerEntity.getMinAmbientSoundDelay();
        }
    }

    @Override
    public void onSellingItem(ItemStack itemStack) {
        // TODO: play sound
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putUuid("Owner", ownerUuid);
        nbt.putString("OwnerName", ownerName);
        VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, villagerStyle)
            .resultOrPartial(LOGGER::error)
            .ifPresent((nbtElement -> nbt.put("VillagerStyle", nbtElement)));

        if (villagerUuid != null) {
            nbt.putUuid("AssignedVillager", villagerUuid);
        }

        ShopOfferList offers = getOffers();
        if (!offers.isEmpty()) {
            nbt.put("Offers", offers.toNbt());
        }

        Inventories.writeNbt(nbt, items);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        ownerUuid = nbt.getUuid("Owner");
        if (nbt.contains("OwnerName", NbtElement.STRING_TYPE)) {
            ownerName = nbt.getString("OwnerName");
        } else {
            ownerName = "someone else";
        }

        VillagerData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, nbt.get("VillagerStyle")))
            .resultOrPartial(LOGGER::error)
            .ifPresent(villagerData -> villagerStyle = villagerData);

        villagerUuid = nbt.getUuid("AssignedVillager");

        if (nbt.contains("Offers", NbtElement.LIST_TYPE)) {
            offers = ShopOfferList.fromNbt(nbt.getList("Offers", NbtElement.COMPOUND_TYPE));
        }

        Inventories.readNbt(nbt, items);
    }
}
