package dev.bohush.economy.block;

import dev.bohush.economy.Economy;
import dev.bohush.economy.block.entity.ShopBlockEntity;
import dev.bohush.economy.item.CoinPileItem;
import dev.bohush.economy.shop.ShopOfferList;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShopBlock extends BlockWithEntity {
    public static final Identifier ID = new Identifier(Economy.MOD_ID, "shop");

    public ShopBlock() {
        super(FabricBlockSettings.of(Material.WOOD).strength(2.5F).sounds(BlockSoundGroup.WOOD));
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShopBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
        if (screenHandlerFactory != null) {
            player.openHandledScreen(screenHandlerFactory);
        }

        return ActionResult.CONSUME;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.isClient) {
            return;
        }

        if (!(placer instanceof PlayerEntity player)) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ShopBlockEntity shopBlockEntity) {
            shopBlockEntity.initialize(player);

            if (itemStack.hasCustomName()) {
                shopBlockEntity.setCustomName(itemStack.getName());
            }

            var lookDirection = placer.getHorizontalFacing().getOpposite();
            shopBlockEntity.spawnVillager((ServerWorld) world, lookDirection);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ShopBlockEntity shopBlockEntity) {
                if (!world.isClient) {
                    var storage = shopBlockEntity.getStorage();
                    ItemScatterer.spawn(world, pos, storage);
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), CoinPileItem.createStack(storage.getCoins()));
                    shopBlockEntity.removeVillager();
                }
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ShopBlockEntity shopBlockEntity && !shopBlockEntity.getOwnerUuid().equals(player.getUuid())) {
            return 0;
        }

        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        var nbt = stack.getSubNbt("BlockEntityTag");
        if (nbt == null) {
            return;
        }

        // Show up to 4 selling items
        if (nbt.contains("Offers", NbtElement.LIST_TYPE)) {
            var offers = ShopOfferList.fromNbt(nbt.getList("Offers", NbtElement.COMPOUND_TYPE));

            int count = Math.min(4, offers.size());
            for (int i = 0; i < count; i++) {
                var sellingItem = offers.get(i).getSellItem();
                tooltip.add(sellingItem.getName().shallowCopy());
            }

            if (offers.size() > 4) {
                tooltip.add(new TranslatableText("container.shulkerBox.more", offers.size() - 4).formatted(Formatting.ITALIC));
            }
        }
    }
}
