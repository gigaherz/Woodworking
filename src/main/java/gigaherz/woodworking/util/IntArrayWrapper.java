package gigaherz.woodworking.util;

import net.minecraft.world.inventory.ContainerData;

public class IntArrayWrapper implements ContainerData
{
    private final int[] array;

    public IntArrayWrapper(int[] values)
    {
        this.array = values;
    }

    @Override
    public int get(int index)
    {
        return array[index];
    }

    @Override
    public void set(int index, int value)
    {
        array[index] = value;
    }

    @Override
    public int getCount()
    {
        return 4;
    }
}