package gigaherz.woodworking.sawmill;

import gigaherz.woodworking.WoodworkingTileEntityTypes;
import gigaherz.woodworking.api.ChoppingContext;
import gigaherz.woodworking.api.ChoppingRecipe;
import gigaherz.woodworking.sawmill.gui.SawmillContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class SawmillTileEntity extends BlockEntity implements ContainerData, MenuProvider
{
    public static RegistryObject<BlockEntityType<SawmillTileEntity>> TYPE = WoodworkingTileEntityTypes.SAWMILL_RACK_TILE_ENTITY_TYPE;

    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> ITEMS_CAP;

    public final ItemStackHandler inventory = new ItemStackHandler(3)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            setChanged();
            needRefreshRecipe = true;
        }
    };
    private final RangedWrapper top = new RangedWrapper(inventory, 0, 1);
    private final RangedWrapper sides = new RangedWrapper(inventory, 1, 2)
    {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if (!AbstractFurnaceBlockEntity.isFuel(stack))
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
    };
    private final RangedWrapper bottom = new RangedWrapper(inventory, 2, 3)
    {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            return stack;
        }
    };

    private final LazyOptional<IItemHandler> combined_provider = LazyOptional.of(() -> inventory);
    private final LazyOptional<IItemHandler> top_provider = LazyOptional.of(() -> top);
    private final LazyOptional<IItemHandler> sides_provider = LazyOptional.of(() -> sides);
    private final LazyOptional<IItemHandler> bottom_provider = LazyOptional.of(() -> bottom);

    private static final Random RANDOM = new Random();

    private int remainingBurnTime;
    private int totalBurnTime;
    private int cookTime;
    private int totalCookTime;

    private boolean needRefreshRecipe = true;

    public SawmillTileEntity(BlockPos pos, BlockState state)
    {
        super(TYPE.get(), pos, state);
    }

    public boolean isBurning()
    {
        return remainingBurnTime > 0;
    }

    public ItemStackHandler getInventory()
    {
        return inventory;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == ITEMS_CAP)
        {
            if (facing == Direction.UP) return top_provider.cast();
            if (facing == Direction.DOWN) return bottom_provider.cast();
            if (facing != null) return sides_provider.cast();
            return combined_provider.cast();
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);

        inventory.deserializeNBT(compound.getCompound("Items"));

        remainingBurnTime = compound.getInt("BurnTime");
        cookTime = compound.getInt("CookTime");
        needRefreshRecipe = true;
    }

    @Override
    public CompoundTag save(CompoundTag compound)
    {
        compound = super.save(compound);

        compound.put("Items", inventory.serializeNBT());

        compound.putInt("BurnTime", (short) this.remainingBurnTime);
        compound.putInt("CookTime", (short) this.cookTime);

        return compound;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        // Ignore. We have nothing to sync.
    }

    public static int getSawmillTime(Level world, ItemStack stack)
    {
        return ChoppingRecipe.getRecipe(world, null, stack).map(recipe -> recipe.getSawingTime()).orElse(0);
    }

    public void tickServer()
    {
        boolean changes = false;

        if (needRefreshRecipe)
        {
            totalBurnTime = ForgeHooks.getBurnTime(inventory.getStackInSlot(1), ChoppingRecipe.CHOPPING);
            totalCookTime = getSawmillTime(level, inventory.getStackInSlot(0));
            needRefreshRecipe = false;
        }

        if (this.isBurning())
        {
            --this.remainingBurnTime;
        }

        ItemStack fuel = this.inventory.getStackInSlot(1);

        if (this.isBurning() || !fuel.isEmpty())
        {
            ChoppingContext ctx = new ChoppingContext(inventory, null, () -> Vec3.atCenterOf(worldPosition), null, 0, RANDOM);
            changes |= ChoppingRecipe.getRecipe(level, ctx).map(choppingRecipe -> {
                boolean changes2 = false;
                if (!this.isBurning() && this.canWork(ctx, choppingRecipe))
                {
                    this.totalBurnTime = ForgeHooks.getBurnTime(fuel, ChoppingRecipe.CHOPPING);
                    this.remainingBurnTime = this.totalBurnTime;

                    if (this.isBurning())
                    {
                        changes2 = true;

                        if (!fuel.isEmpty())
                        {
                            Item item = fuel.getItem();
                            fuel.shrink(1);

                            if (fuel.isEmpty())
                            {
                                ItemStack containerItem = item.getContainerItem(fuel);
                                this.inventory.setStackInSlot(1, containerItem);
                            }
                        }
                    }
                }

                if (this.isBurning() && this.canWork(ctx, choppingRecipe))
                {
                    ++this.cookTime;

                    if (this.totalCookTime == 0)
                    {
                        this.totalCookTime = choppingRecipe.getSawingTime();
                    }

                    if (this.cookTime >= this.totalCookTime)
                    {
                        this.cookTime = 0;
                        this.totalCookTime = choppingRecipe.getSawingTime();
                        this.processItem(ctx, choppingRecipe);
                        changes2 = true;
                    }
                }
                else
                {
                    this.cookTime = 0;
                }

                return changes2;
            }).orElse(false);
        }
        if (!this.isBurning() && this.cookTime > 0)
        {
            this.cookTime = Mth.clamp(this.cookTime - 2, 0, this.totalCookTime);
        }

        BlockState state = getBlockState();
        if (state.getValue(SawmillBlock.POWERED) != this.isBurning())
        {
            state = state.setValue(SawmillBlock.POWERED, this.isBurning());
            level.setBlockAndUpdate(worldPosition, state);
        }

        if (changes)
        {
            this.setChanged();
        }
    }

    private boolean canWork(ChoppingContext ctx, ChoppingRecipe choppingRecipe)
    {
        return getResult(ctx, choppingRecipe).getCount() > 0;
    }

    private void processItem(ChoppingContext ctx, ChoppingRecipe recipe)
    {
        ItemStack input = inventory.getStackInSlot(0);

        if (input.isEmpty())
            return;

        ItemStack result = getResult(ctx, recipe);
        if (result.getCount() <= 0)
            return;

        inventory.insertItem(2, result, false);

        input.shrink(1);
    }

    private ItemStack getResult(ChoppingContext ctx, @Nullable ChoppingRecipe choppingRecipe)
    {
        if (choppingRecipe == null)
            return ItemStack.EMPTY;

        ItemStack result = choppingRecipe.assemble(ctx);

        ItemStack output = inventory.getStackInSlot(2);

        int max = Math.min(inventory.getSlotLimit(2), output.getMaxStackSize());

        int space = max - output.getCount();

        if (space < result.getCount())
            return ItemStack.EMPTY;

        return result;
    }

    @Override
    public int get(int index)
    {
        switch (index)
        {
            case 0:
                return remainingBurnTime;
            case 1:
                return totalBurnTime;
            case 2:
                return cookTime;
            case 3:
                return totalCookTime;
        }
        return 0;
    }

    @Override
    public void set(int index, int value)
    {
        switch (index)
        {
            case 0:
                remainingBurnTime = value;
            case 1:
                totalBurnTime = value;
            case 2:
                cookTime = value;
            case 3:
                totalCookTime = value;
        }
    }

    @Override
    public int getCount()
    {
        return 4;
    }

    @Override
    public Component getDisplayName()
    {
        return new TranslatableComponent("container.sawmill.title");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        return new SawmillContainer(windowId, this, playerInventory);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, SawmillTileEntity e)
    {
        e.tickServer();
    }
}