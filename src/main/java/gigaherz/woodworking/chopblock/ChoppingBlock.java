package gigaherz.woodworking.chopblock;

import gigaherz.woodworking.ConfigManager;
import gigaherz.woodworking.WoodworkingMod;
import gigaherz.woodworking.api.ChoppingRecipe;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = WoodworkingMod.MODID)
public class ChoppingBlock extends Block implements EntityBlock
{
    protected static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    private final Supplier<BlockState> breaksInto;

    public ChoppingBlock(@Nullable Supplier<BlockState> breaksInto, Properties properties)
    {
        super(properties);
        this.breaksInto = breaksInto != null ? breaksInto : (() -> Blocks.AIR.defaultBlockState());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return new ChoppingBlockTileEntity(blockPos, blockState);
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Deprecated
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockRayTraceResult)
    {
        ItemStack heldItem = player.getItemInHand(hand);

        if (worldIn.isClientSide)
        {
            return (heldItem.getCount() <= 0) || ChoppingRecipe.getRecipe(worldIn, pos, heldItem).isPresent() ?
                    InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        BlockEntity tileEntity = worldIn.getBlockEntity(pos);

        if (!(tileEntity instanceof ChoppingBlockTileEntity) || player.isShiftKeyDown())
            return InteractionResult.PASS;

        ChoppingBlockTileEntity chopper = (ChoppingBlockTileEntity) tileEntity;

        if (heldItem.getCount() <= 0)
        {
            ItemStack extracted = chopper.getSlotInventory().extractItem(0, 1, false);
            if (extracted.getCount() > 0)
            {
                ItemHandlerHelper.giveItemToPlayer(player, extracted);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        }

        if (ChoppingRecipe.getRecipe(worldIn, pos, heldItem)
                .isPresent())
        {
            ItemStack remaining = chopper.getSlotInventory().insertItem(0, heldItem, false);
            if (!player.isCreative())
            {
                if (remaining.getCount() > 0)
                {
                    player.setItemInHand(hand, remaining);
                }
                else
                {
                    player.setItemInHand(hand, ItemStack.EMPTY);
                }
            }
            return remaining.getCount() < heldItem.getCount() ?
                    InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    @SubscribeEvent
    public static void interactEvent(PlayerInteractEvent.LeftClickBlock event)
    {
        Player player = event.getPlayer();
        Level world = player.level;
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof ChoppingBlock)
        {
            if (((ChoppingBlock) block).interceptClick(world, pos, state, player))
                event.setCanceled(true);
        }
    }

    private boolean interceptClick(Level worldIn, BlockPos pos, BlockState state, Player playerIn)
    {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);

        if (!(tileentity instanceof ChoppingBlockTileEntity chopper))
            return false;

        if (chopper.getSlotInventory().getStackInSlot(0).getCount() <= 0)
            return false;

        if (worldIn.isClientSide)
            return true;

        ItemStack heldItem = playerIn.getItemInHand(InteractionHand.MAIN_HAND);

        boolean canAxe = heldItem.getItem().canPerformAction(heldItem, ToolActions.AXE_DIG);
        Tier tier = canAxe && heldItem.getItem() instanceof TieredItem tieredItem ? tieredItem.getTier() : null;
        InteractionResultHolder<ItemStack> result = chopper.chop(playerIn, tier, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, heldItem));
        if (result.getResult() == InteractionResult.SUCCESS)
        {
            if (worldIn.random.nextFloat() < ConfigManager.SERVER.choppingDegradeChance.get())
            {
                worldIn.setBlockAndUpdate(pos, breaksInto.get());
            }

            if (ConfigManager.SERVER.choppingExhaustion.get() > 0)
                playerIn.causeFoodExhaustion(ConfigManager.SERVER.choppingExhaustion.get().floatValue());

            if (heldItem.getCount() > 0 && !playerIn.getAbilities().instabuild)
            {
                heldItem.hurtAndBreak(1, playerIn, (stack) -> {
                    stack.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                });
            }
        }
        if (result.getResult() != InteractionResult.PASS)
        {
            ((ServerLevel) worldIn).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, result.getObject()),
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 8,
                    0, 0.1, 0, 0.02);
        }

        return true;
    }

    @Deprecated
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (newState.getBlock() != state.getBlock())
        {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);

            if (tileentity instanceof ChoppingBlockTileEntity)
            {
                dropInventoryItems(worldIn, pos, ((ChoppingBlockTileEntity) tileentity).getSlotInventory());
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    public static void dropInventoryItems(Level worldIn, BlockPos pos, IItemHandler inventory)
    {
        for (int i = 0; i < inventory.getSlots(); ++i)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (itemstack.getCount() > 0)
            {
                Containers.dropItemStack(worldIn, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), itemstack);
            }
        }
    }
}