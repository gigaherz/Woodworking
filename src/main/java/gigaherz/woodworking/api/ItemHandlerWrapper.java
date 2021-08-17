package gigaherz.woodworking.api;

import com.google.common.collect.AbstractIterator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Supplier;

public class ItemHandlerWrapper implements IInventory, Iterable<ItemStack>
{
    protected final IItemHandlerModifiable inner;

    @Nullable
    protected final Supplier<Vector3d> location;
    protected final int distance;

    public ItemHandlerWrapper(IItemHandlerModifiable inner)
    {
        this(inner, null, 0);
    }

    public ItemHandlerWrapper(IItemHandlerModifiable inner, @Nullable Supplier<Vector3d> location, int distance)
    {
        this.inner = inner;
        this.distance = distance;
        this.location = location;
    }

    @Override
    public int getSizeInventory()
    {
        return inner.getSlots();
    }

    @Override
    public boolean isEmpty()
    {
        for (int i = 0; i < inner.getSlots(); i++)
        {
            if (inner.getStackInSlot(i).getCount() > 0)
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inner.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        return inner.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return inner.extractItem(index, 64, false);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        inner.setStackInSlot(index, stack);
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        if (location == null)
            return true;
        return player.getPositionVec().distanceTo(location.get()) <= distance;
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < inner.getSlots(); i++)
        {
            inner.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public IItemHandlerModifiable getInner()
    {
        return inner;
    }

    @Override
    public Iterator<ItemStack> iterator()
    {
        return new AbstractIterator<ItemStack>()
        {
            int current = 0;

            @Override
            protected ItemStack computeNext()
            {
                if (current >= getSizeInventory())
                {
                    return endOfData();
                }
                ItemStack stack = getStackInSlot(current++);
                return stack;
            }
        };
    }
}