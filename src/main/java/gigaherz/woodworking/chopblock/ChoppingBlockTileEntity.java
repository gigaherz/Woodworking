package gigaherz.woodworking.chopblock;

import gigaherz.woodworking.WoodworkingTileEntityTypes;
import gigaherz.woodworking.api.ChoppingContext;
import gigaherz.woodworking.api.ChoppingRecipe;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

public class ChoppingBlockTileEntity extends BlockEntity
{
    public static final RegistryObject<BlockEntityType<ChoppingBlockTileEntity>> TYPE = WoodworkingTileEntityTypes.CHOPPING_BLOCK_TILE_ENTITY_TYPE;

    private static final Random RANDOM = new Random();

    private final ItemStackHandler slotInventory = new ItemStackHandler(1)
    {
        @Override
        protected int getStackLimit(int slot, ItemStack stack)
        {
            return 1;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (!ChoppingRecipe.getRecipe(level, null, stack)
                    .isPresent())
                return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            breakingProgress = 0;
            if (level != null)
            {
                BlockState state = level.getBlockState(worldPosition);
                level.sendBlockUpdated(worldPosition, state, state, 3);
            }
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> slotInventoryGetter = LazyOptional.of(() -> slotInventory);

    // measured in the number of ticks it will take to return to 0
    private int breakingProgress = 0;

    public ChoppingBlockTileEntity(BlockPos pos, BlockState state)
    {
        super(TYPE.get(), pos, state);
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return slotInventoryGetter.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);
        slotInventory.deserializeNBT(compound.getCompound("Inventory"));
    }

    @Override
    public CompoundTag save(CompoundTag compound)
    {
        compound = super.save(compound);
        compound.put("Inventory", slotInventory.serializeNBT());
        return compound;
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag compound = new CompoundTag();
        compound.put("Item", slotInventory.getStackInSlot(0).serializeNBT());
        return compound;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        slotInventory.setStackInSlot(0, ItemStack.of(tag.getCompound("Item")));
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return new ClientboundBlockEntityDataPacket(worldPosition, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        handleUpdateTag(pkt.getTag());
    }

    public InteractionResultHolder<ItemStack> chop(Player player, Tier axeLevel, int fortune)
    {
        InteractionResult completed = InteractionResult.PASS;
        ItemStack containedItem = slotInventory.getStackInSlot(0).copy();
        if (containedItem.getCount() > 0)
        {
            ChoppingContext ctx = new ChoppingContext(slotInventory, player, () -> Vec3.atCenterOf(worldPosition), axeLevel, fortune, RANDOM);

            Optional<ChoppingRecipe> foundRecipe = ChoppingRecipe.getRecipe(level, ctx);

            completed = foundRecipe.map(recipe -> {

                InteractionResult completed2 = InteractionResult.PASS;

                breakingProgress += recipe.getHitProgress(axeLevel);
                if (breakingProgress >= 200)
                {
                    if (!level.isClientSide)
                    {
                        ItemStack out = recipe.assemble(ctx);

                        if (out.getCount() > 0)
                        {
                            //ItemHandlerHelper.giveItemToPlayer(player, out);
                            ItemHandlerHelper.giveItemToPlayer(player, out);

                            completed2 = InteractionResult.SUCCESS;
                        }
                        else
                        {
                            completed2 = InteractionResult.FAIL;
                        }
                    }
                    level.playSound(player, worldPosition, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                    slotInventory.setStackInSlot(0, ItemStack.EMPTY);
                    breakingProgress = 0;
                }

                BlockState state = level.getBlockState(worldPosition);
                level.sendBlockUpdated(worldPosition, state, state, 3);

                return completed2;
            }).orElse(InteractionResult.PASS);
        }
        return new InteractionResultHolder<>(completed, containedItem);
    }

    public static void spawnItemStack(Level worldIn, double x, double y, double z, ItemStack stack)
    {
        while (stack.getCount() > 0)
        {
            int i = /*RANDOM.nextInt(3) +*/ 1;

            if (i > stack.getCount())
            {
                i = stack.getCount();
            }

            ItemStack copy = stack.copy();
            copy.setCount(i);
            stack.grow(-i);

            Block.popResource(worldIn, new BlockPos(x, y, z), stack);
        }
    }

    public ItemStackHandler getSlotInventory()
    {
        return slotInventory;
    }

    public int getBreakStage()
    {
        if (breakingProgress <= 0)
            return -1;
        return breakingProgress * 10 / 200;
    }
}