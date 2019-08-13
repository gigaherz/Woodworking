package gigaherz.survivalist.misc;

import com.google.common.collect.ImmutableList;
import gigaherz.survivalist.Survivalist;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.Random;

public class StringEventHandling
{
    private final Random rnd = new Random();

    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new StringEventHandling());
    }

    @SubscribeEvent
    public void entityDrops(LivingDropsEvent ev)
    {
        Entity entity = ev.getEntity();
        if (!(entity instanceof SheepEntity))
            return;

        Collection<ItemEntity> drops = ev.getDrops();
        if (drops instanceof ImmutableList)
        {
            Survivalist.logger.warn("WARNING: Some mod is returning an ImmutableList, replacing drops will NOT be possible.");
            return;
        }

        if (rnd.nextFloat() < 0.25f)
            drops.add(new ItemEntity(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ, new ItemStack(Items.STRING)));
    }
}
