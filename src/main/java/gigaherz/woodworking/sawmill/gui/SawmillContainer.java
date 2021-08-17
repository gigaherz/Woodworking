package gigaherz.woodworking.sawmill.gui;

import com.google.common.collect.Lists;
import gigaherz.woodworking.api.ChoppingContext;
import gigaherz.woodworking.api.ChoppingRecipe;
import gigaherz.woodworking.sawmill.SawmillTileEntity;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class SawmillContainer extends AbstractContainerMenu
{
    private static final Random RANDOM = new Random();

    @ObjectHolder("woodworking:sawmill")
    public static MenuType<SawmillContainer> TYPE;

    private final ChoppingContext wrappedInventory;
    private final Level world;
    private final BlockPos pos;
    private ContainerData fields;

    public SawmillContainer(int windowId, Inventory playerInventory)
    {
        this(windowId, playerInventory, new ItemStackHandler(3), null, new SimpleContainerData(4));
    }

    public SawmillContainer(int windowId, SawmillTileEntity tileEntity, Inventory playerInventory)
    {
        this(windowId, playerInventory, tileEntity.inventory, tileEntity.getBlockPos(), tileEntity);
    }

    public SawmillContainer(int windowId, Inventory playerInventory, IItemHandlerModifiable inventory, @Nullable BlockPos pos, ContainerData dryTimes)
    {
        super(TYPE, windowId);

        fields = dryTimes;

        wrappedInventory = new ChoppingContext(inventory, null, null, null, 0, RANDOM);
        world = playerInventory.player.level;
        this.pos = pos;

        addSlot(new SlotItemHandler(inventory, 0, 56, 17));
        addSlot(new SawmillFuelSlot(inventory, 1, 56, 53));
        addSlot(new SawmillOutputSlot(inventory, 2, 116, 35));

        bindPlayerInventory(playerInventory);

        addDataSlots(fields);
    }

    private void bindPlayerInventory(Inventory playerInventory)
    {
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k)
        {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    public int getRemainingBurnTime()
    {
        return fields.get(0);
    }

    public int getTotalBurnTime()
    {
        return fields.get(1);
    }

    public int getCookTime()
    {
        return fields.get(2);
    }

    public int getTotalCookTime()
    {
        return fields.get(3);
    }

    public boolean isBurning()
    {
        return getRemainingBurnTime() > 0;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem())
        {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index == 2)
            {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            }
            else if (index != 1 && index != 0)
            {
                if (ChoppingRecipe.getRecipe(world, pos, itemstack1)
                        .isPresent())
                {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (AbstractFurnaceBlockEntity.isFuel(itemstack1))
                {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (index >= 3 && index < 30)
                {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(itemstack1, 3, 39, false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }
}