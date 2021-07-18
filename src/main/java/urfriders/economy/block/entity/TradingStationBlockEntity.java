package urfriders.economy.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
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
import urfriders.economy.block.TradingStationBlock;
import urfriders.economy.inventory.ImplementedInventory;
import urfriders.economy.screen.TradingStationScreenHandler;

public class TradingStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);

    public TradingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRADING_STATION, pos, state);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, items);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
    }

    public void spawnVillager(ServerWorld world) {
        if (items.get(0).isEmpty() || items.get(1).isEmpty()) {
            System.out.println("TradingStationBlockEntity: One or more slots are empty");
            return;
        }

        VillagerEntity villager = EntityType.VILLAGER.create(world);
        if (villager != null) {
            // Base villager data
            villager.setCustomName(new LiteralText("Player's trades"));
            villager.setVillagerData(new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 99));

            // Trades
            TradeOfferList offers = new TradeOfferList();
            offers.add(new TradeOffer(items.get(0).copy(), items.get(1).copy(), 1, 0, 0));
            villager.setOffers(offers);

            // Position and rotation
            float yaw = world.getBlockState(pos).get(TradingStationBlock.FACING).asRotation();
            villager.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            villager.setYaw(yaw);

            // Extra
            villager.setInvulnerable(true);
            villager.setPersistent();
            villager.setAiDisabled(true);

            boolean success = world.spawnEntity(villager);
            if (success) {
                System.out.println("TradingStationBlockEntity: Spawned villager " + villager.getUuidAsString());
            } else {
                System.out.println("TradingStationBlockEntity: Could not spawn villager");
            }
        }
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
        return new TradingStationScreenHandler(syncId, playerInventory, this);
    }
}
