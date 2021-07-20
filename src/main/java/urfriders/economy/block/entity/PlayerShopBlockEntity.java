package urfriders.economy.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.*;
import org.jetbrains.annotations.Nullable;
import urfriders.economy.block.PlayerShopBlock;
import urfriders.economy.entity.ModEntities;
import urfriders.economy.entity.ShopVillagerEntity;
import urfriders.economy.inventory.ImplementedInventory;
import urfriders.economy.screen.PlayerShopScreenHandler;

import java.util.UUID;

public class PlayerShopBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);

    private UUID ownerUuid;
    private String ownerName;
    private UUID villagerUuid;

    public PlayerShopBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLAYER_SHOP, pos, state);
        ownerName = "someone else";
    }

    public void setOwner(PlayerEntity player) {
        ownerUuid = player.getUuid();
        ownerName = player.getName().asString();
    }

    public void updateVillager(ServerWorld world, PlayerEntity player) {
        if (!player.getUuid().equals(ownerUuid)) {
            String playerName = player.getName().asString();
            if (player.isCreativeLevelTwoOp()) {
                System.out.printf("PlayerShop: %s is updating someone else's shop.%n", playerName);
            } else {
                System.out.printf("PlayerShop: %s tried to update someone else's shop.%n", playerName);
                return;
            }
        }

        ShopVillagerEntity villager = getVillager(world);
        if (villager == null) {
            System.out.println("PlayerShop: Villager not found " + villagerUuid);
            return;
        }

        // villager.setTraders(this.trades);
        ItemStack itemBuy = items.get(0).copy();
        ItemStack itemSell = items.get(1).copy();

        if (itemBuy.isEmpty() || itemSell.isEmpty()) {
            villager.setOffers(new TradeOfferList());
            return;
        }

        TradeOfferList offers = new TradeOfferList();
        offers.add(new TradeOffer(itemBuy, itemSell, 1, 0, 0));
//        offers.add(new TradeOffer(new ItemStack(Items.DIRT), new ItemStack(Items.DIAMOND), 0, 0, 0));
        villager.setOffers(offers);
    }

    public void spawnVillager(ServerWorld world) {
        if (villagerUuid != null) {
            System.out.println("PlayerShop: Villager is already spawned");
            return;
        }

        if (ownerUuid == null) {
            System.out.println("PlayerShop: Shop has no owner");
            return;
        }

        ShopVillagerEntity villager = ModEntities.SHOP_VILLAGER.create(world);
        if (villager == null) {
            System.out.println("PlayerShop: Could not create villager");
            return;
        }

        // Base villager data
        PlayerEntity player = world.getPlayerByUuid(ownerUuid);
        if (player == null) {
            System.out.println("PlayerShop: Player not found " + ownerUuid.toString());
            return;
        }

        villager.setCustomName(new LiteralText(player.getDisplayName().asString().concat("'s Shop")));
        villager.setVillagerData(new VillagerData(VillagerType.PLAINS, VillagerProfession.ARMORER, 99));
        villager.setShop(this);

        // Position and rotation
        float yaw = world.getBlockState(pos).get(PlayerShopBlock.FACING).asRotation();
        villager.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        villager.setYaw(yaw);

        // Extra
        villager.setInvulnerable(true);
        villager.setPersistent();
        villager.setAiDisabled(true);

        boolean success = world.spawnEntity(villager);
        if (success) {
            System.out.println("PlayerShop: Spawned villager " + villager.getUuidAsString());
            villagerUuid = villager.getUuid();
            updateVillager(world, player);
        } else {
            System.out.println("PlayerShop: Could not spawn villager");
        }
    }

    public void removeVillager(ServerWorld world) {
        ShopVillagerEntity villager = getVillager(world);
        if (villager != null) {
//            villager.kill();
            villager.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private ShopVillagerEntity getVillager(ServerWorld world) {
        Entity entity = world.getEntity(villagerUuid);
        if (entity instanceof ShopVillagerEntity villager) {
            return villager;
        }

        System.out.println("PlayerShop: Villager not found " + villagerUuid);
        return null;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (!player.getUuid().equals(ownerUuid) && !(player.isCreativeLevelTwoOp() || player.isSpectator())) {
            // TODO: Update ownerName
            player.sendMessage(new TranslatableText("player_shop.differentOwner", ownerName), true);
            return null;
        }

        // TODO: Disable the villager UI while the screen is open
        // This is to prevent any potential duplication glitches.
        return new PlayerShopScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putUuid("ownerUuid", ownerUuid);
        nbt.putString("ownerName", ownerName);
        nbt.putUuid("villagerUuid", villagerUuid);
        Inventories.writeNbt(nbt, items);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        ownerUuid = nbt.getUuid("ownerUuid");
        ownerName = nbt.getString("ownerName");
        villagerUuid = nbt.getUuid("villagerUuid");
        Inventories.readNbt(nbt, items);
    }
}
